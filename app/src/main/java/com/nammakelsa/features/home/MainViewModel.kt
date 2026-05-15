package com.nammakelsa.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammakelsa.core.datastore.PreferencesManager
import com.nammakelsa.data.repository.WorkerRepository
import com.nammakelsa.domain.models.Worker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val workers: List<Worker> = emptyList(),
    val error: String? = null,
    val userName: String = "",
    val userPhone: String = "",
    val userEmail: String = "",
    val userAddress: String = "",
    val userCity: String = "",
    val userRole: String = ""
)

@HiltViewModel
class MainViewModel @Inject constructor(private val prefs: PreferencesManager, private val workerRepo: WorkerRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val workers = workerRepo.getAllAvailableWorkers()
            val name = prefs.getUserName()
            val phone = prefs.getUserPhone()
            val email = prefs.getUserEmail()
            val address = prefs.getUserAddress()
            val city = prefs.getUserCity()
            val role = prefs.getUserRole()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                workers = workers,
                userName = name,
                userPhone = phone,
                userEmail = email,
                userAddress = address,
                userCity = city,
                userRole = role
            )
        }
    }

    fun loadWorkers() {
        viewModelScope.launch {
            val workers = workerRepo.getAllAvailableWorkers()
            _uiState.value = _uiState.value.copy(workers = workers)
        }
    }

    fun searchWorkers(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val results = if (query.isBlank()) {
                workerRepo.getAllAvailableWorkers()
            } else {
                workerRepo.searchWorkers(query)
            }
            _uiState.value = _uiState.value.copy(isLoading = false, workers = results)
        }
    }

    suspend fun getUserRole(): String = prefs.getUserRole()
    suspend fun getUserId(): String = prefs.getUserId()
}
