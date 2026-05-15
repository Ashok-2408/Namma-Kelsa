package com.nammakelsa.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammakelsa.core.datastore.PreferencesManager
import com.nammakelsa.data.repository.ChatRepository
import com.nammakelsa.data.repository.WorkerRepository
import com.nammakelsa.domain.models.Chat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val isLoading: Boolean = false,
    val chats: List<Chat> = emptyList(),
    val workerNames: Map<String, String> = emptyMap(),
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepo: ChatRepository,
    private val workerRepo: WorkerRepository,
    private val prefs: PreferencesManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    init { loadChats() }

    fun loadChats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val userId = prefs.getUserId()
            if (userId.isEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return@launch
            }
            try {
                chatRepo.getChatsFlow(userId).collect { chats ->
                    val nameMap = mutableMapOf<String, String>()
                    chats.forEach { chat ->
                        val otherId = chat.participants.find { it != userId } ?: ""
                        if (otherId.isNotEmpty() && !nameMap.containsKey(chat.id)) {
                            val worker = workerRepo.getWorkerById(otherId)
                            nameMap[chat.id] = worker?.name ?: "User"
                        }
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, chats = chats, workerNames = nameMap)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
