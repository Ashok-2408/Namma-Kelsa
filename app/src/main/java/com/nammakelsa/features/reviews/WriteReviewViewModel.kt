package com.nammakelsa.features.reviews

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammakelsa.core.datastore.PreferencesManager
import com.nammakelsa.data.repository.ReviewRepository
import com.nammakelsa.domain.models.Review
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WriteReviewUiState(
    val rating: Int = 0,
    val comment: String = "",
    val isSubmitting: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WriteReviewViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val reviewRepo: ReviewRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    val workerId: String = savedStateHandle["workerId"] ?: ""
    val bookingId: String = savedStateHandle["bookingId"] ?: ""

    private val _uiState = MutableStateFlow(WriteReviewUiState())
    val uiState: StateFlow<WriteReviewUiState> = _uiState

    fun updateRating(rating: Int) {
        _uiState.value = _uiState.value.copy(rating = rating)
    }

    fun updateComment(comment: String) {
        _uiState.value = _uiState.value.copy(comment = comment)
    }

    fun submitReview() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            try {
                val userId = prefs.getUserId()
                val userName = prefs.getUserName()
                val review = Review(
                    bookingId = bookingId,
                    fromUserId = userId,
                    fromUserName = userName.ifEmpty { "Customer" },
                    toUserId = workerId,
                    rating = _uiState.value.rating.toFloat(),
                    comment = _uiState.value.comment
                )
                reviewRepo.createReview(review)
                _uiState.value = _uiState.value.copy(isSubmitting = false, success = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, error = e.message)
            }
        }
    }
}
