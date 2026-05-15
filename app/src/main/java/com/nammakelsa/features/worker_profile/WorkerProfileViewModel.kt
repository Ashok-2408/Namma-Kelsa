package com.nammakelsa.features.worker_profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammakelsa.core.datastore.PreferencesManager
import com.nammakelsa.core.utils.Constants
import com.nammakelsa.core.utils.Resource
import com.nammakelsa.data.repository.WorkerRepository
import com.nammakelsa.domain.models.Worker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkerProfileUiState(
    val isLoading: Boolean = false,
    val worker: Worker? = null,
    val userName: String = "",
    val userPhone: String = "",
    val userEmail: String = "",
    val userAddress: String = "",
    val userCity: String = "",
    val userRole: String = "",
    val currentLanguage: String = "en",
    val isDarkMode: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WorkerProfileViewModel @Inject constructor(
    private val workerRepo: WorkerRepository,
    private val prefs: PreferencesManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkerProfileUiState())
    val uiState: StateFlow<WorkerProfileUiState> = _uiState

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val userId = prefs.getUserId()
            val userName = prefs.getUserName()
            val userPhone = prefs.getUserPhone()
            val userEmail = prefs.getUserEmail()
            val userAddress = prefs.getUserAddress()
            val userCity = prefs.getUserCity()
            val userRole = prefs.getUserRole()
            val lang = prefs.getAppLanguage()
            val darkMode = prefs.isDarkMode.first() ?: false

            if (userRole == Constants.ROLE_WORKER && userId.isNotEmpty()) {
                try {
                    val worker = workerRepo.getWorkerById(userId)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, worker = worker,
                        userName = userName, userPhone = userPhone,
                        userEmail = userEmail, userAddress = userAddress,
                        userCity = userCity, userRole = userRole,
                        currentLanguage = lang,
                        isDarkMode = darkMode
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, error = e.message,
                        userName = userName, userPhone = userPhone,
                        userEmail = userEmail, userAddress = userAddress,
                        userCity = userCity, userRole = userRole,
                        currentLanguage = lang,
                        isDarkMode = darkMode
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userName = userName, userPhone = userPhone,
                    userEmail = userEmail, userAddress = userAddress,
                    userCity = userCity, userRole = userRole,
                    currentLanguage = lang,
                    isDarkMode = darkMode,
                    worker = null
                )
            }
        }
    }

    suspend fun toggleDarkMode(enabled: Boolean) {
        prefs.setDarkMode(enabled)
        loadProfile()
    }

    fun saveProfile(name: String, phone: String, skillType: String, dailyRate: Int, city: String, experience: Int, bio: String, galleryUris: List<Uri> = emptyList()) {
        viewModelScope.launch {
            val userId = prefs.getUserId()
            prefs.saveUserName(name)
            prefs.saveUserPhone(phone)
            prefs.saveUserCity(city)
            prefs.saveUserRole(Constants.ROLE_WORKER)

            val existingWorker = workerRepo.getWorkerById(userId)
            val existingPhotos = existingWorker?.galleryPhotos ?: emptyList()
            val newUrls = mutableListOf<String>()
            galleryUris.forEachIndexed { i, uri ->
                val url = workerRepo.uploadGalleryPhoto(userId, uri, existingPhotos.size + i)
                if (url != null) newUrls.add(url)
            }
            val worker = Worker(
                uid = userId, name = name, phone = phone, skillType = skillType,
                dailyRate = dailyRate, locationCity = city, experience = experience,
                bio = bio, isAvailable = true,
                galleryPhotos = existingPhotos + newUrls,
                createdAt = System.currentTimeMillis()
            )
            when (workerRepo.saveWorkerProfile(worker)) {
                is Resource.Success -> { loadProfile() }
                else -> { }
            }
        }
    }

    fun updateUserInfo(name: String, phone: String, email: String, address: String, city: String) {
        viewModelScope.launch {
            prefs.saveUserName(name)
            prefs.saveUserPhone(phone)
            prefs.saveUserEmail(email)
            prefs.saveUserAddress(address)
            prefs.saveUserCity(city)
            loadProfile()
        }
    }

    fun updateAvailability(isAvailable: Boolean) {
        viewModelScope.launch {
            val userId = prefs.getUserId()
            when (workerRepo.updateAvailability(userId, isAvailable)) {
                is Resource.Success -> loadProfile()
                else -> { }
            }
        }
    }

    suspend fun changeLanguage(lang: String) {
        prefs.setAppLanguage(lang)
        loadProfile()
    }
}
