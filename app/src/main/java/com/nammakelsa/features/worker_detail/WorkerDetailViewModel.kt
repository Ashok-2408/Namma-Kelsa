package com.nammakelsa.features.worker_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammakelsa.core.datastore.PreferencesManager
import com.nammakelsa.core.utils.Constants
import com.nammakelsa.data.repository.BookingRepository
import com.nammakelsa.data.repository.ChatRepository
import com.nammakelsa.data.repository.ReviewRepository
import com.nammakelsa.data.repository.WorkerRepository
import com.nammakelsa.domain.models.Booking
import com.nammakelsa.domain.models.Review
import com.nammakelsa.domain.models.Worker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkerDetailUiState(
    val isLoading: Boolean = true,
    val worker: Worker? = null,
    val reviews: List<Review> = emptyList(),
    val error: String? = null,
    val bookingSuccess: Boolean = false
)

@HiltViewModel
class WorkerDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val workerRepo: WorkerRepository,
    private val reviewRepo: ReviewRepository,
    private val bookingRepo: BookingRepository,
    private val chatRepo: ChatRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    val workerId: String = savedStateHandle["workerId"] ?: ""

    private val _uiState = MutableStateFlow(WorkerDetailUiState())
    val uiState: StateFlow<WorkerDetailUiState> = _uiState

    init {
        viewModelScope.launch { loadWorker() }
    }

    private suspend fun loadWorker() {
        if (workerId.isEmpty()) return
        try {
            val w = workerRepo.getWorkerById(workerId)
            _uiState.value = _uiState.value.copy(worker = w, isLoading = false)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
        }
        loadReviews()
    }

    private fun loadReviews() {
        viewModelScope.launch {
            try {
                reviewRepo.getReviewsFlow(workerId).collect { reviews ->
                    _uiState.value = _uiState.value.copy(reviews = reviews)
                }
            } catch (_: Exception) { }
        }
    }

    fun createBooking(description: String, address: String, scheduledDate: Long, scheduledTime: String, agreedRate: Int) {
        viewModelScope.launch {
            try {
                val userId = prefs.getUserId()
                val userName = prefs.getUserName()
                val userPhone = prefs.getUserPhone()
                val w = _uiState.value.worker ?: return@launch
                val booking = Booking(
                    customerId = userId,
                    customerName = userName,
                    customerPhone = userPhone,
                    workerId = w.uid,
                    workerName = w.name,
                    workerPhone = w.phone,
                    skillType = w.skillType,
                    scheduledDate = scheduledDate,
                    scheduledTime = scheduledTime,
                    address = address,
                    description = description,
                    agreedRate = agreedRate,
                    status = Constants.BOOKING_PENDING,
                    createdAt = System.currentTimeMillis()
                )
                bookingRepo.createBooking(booking)

                chatRepo.sendMessage(
                    senderId = userId,
                    receiverId = w.uid,
                    text = "Hi ${w.name}, I booked you for ${w.skillType} service on ${if (scheduledDate > 0) java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(scheduledDate)) else "soon"}. Please check your bookings."
                )

                _uiState.value = _uiState.value.copy(bookingSuccess = true)
            } catch (_: Exception) { }
        }
    }

    fun resetBookingSuccess() {
        _uiState.value = _uiState.value.copy(bookingSuccess = false)
    }
}
