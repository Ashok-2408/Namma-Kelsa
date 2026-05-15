package com.nammakelsa.features.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammakelsa.core.datastore.PreferencesManager
import com.nammakelsa.core.utils.Constants
import com.nammakelsa.core.utils.Resource
import com.nammakelsa.data.repository.BookingRepository
import com.nammakelsa.domain.models.Booking
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookingsUiState(val isLoading: Boolean = false, val bookings: List<Booking> = emptyList(), val userRole: String = "", val error: String? = null)

@HiltViewModel
class BookingsViewModel @Inject constructor(private val bookingRepo: BookingRepository, private val prefs: PreferencesManager) : ViewModel() {
    private val _uiState = MutableStateFlow(BookingsUiState())
    val uiState: StateFlow<BookingsUiState> = _uiState

    init { loadBookings() }

    fun loadBookings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val userId = prefs.getUserId()
            val role = prefs.getUserRole()
            if (userId.isEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return@launch
            }
            _uiState.value = _uiState.value.copy(userRole = role)
            try {
                bookingRepo.getBookingsFlow(userId, role).collect { bookings ->
                    _uiState.value = _uiState.value.copy(isLoading = false, bookings = bookings, error = null)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to load bookings")
            }
        }
    }

    fun updateStatus(bookingId: String, status: String) {
        viewModelScope.launch {
            when (bookingRepo.updateBookingStatus(bookingId, status)) {
                is Resource.Success -> loadBookings()
                is Resource.Error -> { }
                else -> { }
            }
        }
    }
}
