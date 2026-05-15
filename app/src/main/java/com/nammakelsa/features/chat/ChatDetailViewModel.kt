package com.nammakelsa.features.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammakelsa.core.datastore.PreferencesManager
import com.nammakelsa.data.repository.ChatRepository
import com.nammakelsa.data.repository.WorkerRepository
import com.nammakelsa.domain.models.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatDetailUiState(
    val isLoading: Boolean = true,
    val messages: List<Message> = emptyList(),
    val workerName: String = "User",
    val error: String? = null
)

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val chatRepo: ChatRepository,
    private val workerRepo: WorkerRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    private val chatId: String = savedStateHandle["chatId"] ?: ""
    var currentUserId: String = ""
        private set
    private val _uiState = MutableStateFlow(ChatDetailUiState())
    val uiState: StateFlow<ChatDetailUiState> = _uiState
    private var receiverId: String = ""
    private var workerName: String = ""

    private val autoReplies = mapOf(
        "Plumber" to listOf("Sure, I can fix that pipe issue. When are you free?", "I'll bring my tools and check the leakage.", "I charge ₹500 for inspection, then the repair cost."),
        "Electrician" to listOf("I can come tomorrow morning for the wiring.", "Please share the exact location, I'll be there.", "I can fix switches and wiring both."),
        "Carpenter" to listOf("I do furniture repair and new installations.", "Can you share photos of the work needed?", "I'll be available this weekend."),
        "Painter" to listOf("I do both interior and exterior painting.", "I'll bring color samples for you to choose.", "The cost depends on the area size."),
        "Cleaner" to listOf("I provide deep cleaning service for homes.", "I use eco-friendly cleaning products.", "Can I come tomorrow to check the place?"),
        "Mechanic" to listOf("I can repair your vehicle at your location.", "Please describe the issue you're facing.", "I'll bring my diagnostic tools."),
        "Driver" to listOf("I'm available for daily or hourly driving.", "I have a valid license and clean record.", "Let me know the pickup point."),
        "Gardener" to listOf("I can maintain your garden weekly.", "I do planting, trimming, and lawn mowing.", "I'll bring my own gardening tools."),
        "Tiler" to listOf("I do floor and wall tiling work.", "Can you share the area measurements?", "I have references from my previous work.")
    )

    init {
        viewModelScope.launch {
            try {
                currentUserId = prefs.getUserId()
                if (chatId.isEmpty() || currentUserId.isEmpty()) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Invalid chat session")
                    return@launch
                }
                resolveReceiver()
                loadMessages()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to load chat")
            }
        }
    }

    private suspend fun resolveReceiver() {
        val parts = chatId.split("_")
        receiverId = if (parts.size == 2) {
            if (parts[0] == currentUserId) parts[1] else parts[0]
        } else ""
        if (receiverId.isNotEmpty()) {
            try {
                val worker = workerRepo.getWorkerById(receiverId)
                if (worker != null) {
                    workerName = worker.name
                    _uiState.value = _uiState.value.copy(workerName = worker.name)
                    autoReplySkill = worker.skillType
                }
            } catch (_: Exception) { }
        }
    }

    private var autoReplySkill: String = ""

    private fun loadMessages() {
        viewModelScope.launch {
            try {
                chatRepo.getMessagesFlow(chatId).collect { messages ->
                    _uiState.value = _uiState.value.copy(isLoading = false, messages = messages)
                    chatRepo.markAsRead(chatId, currentUserId)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            try {
                chatRepo.sendMessage(currentUserId, receiverId, text)
                triggerAutoReply()
            } catch (_: Exception) { }
        }
    }

    private fun triggerAutoReply() {
        val replies = autoReplies[autoReplySkill] ?: return
        viewModelScope.launch {
            delay(1500)
            val reply = replies.random()
            chatRepo.sendMessage(receiverId, currentUserId, reply)
        }
    }
}
