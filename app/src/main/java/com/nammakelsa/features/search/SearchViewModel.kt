package com.nammakelsa.features.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammakelsa.data.repository.WorkerRepository
import com.nammakelsa.domain.models.Worker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(val isLoading: Boolean = false, val workers: List<Worker> = emptyList(), val error: String? = null)

@HiltViewModel
class SearchViewModel @Inject constructor(private val workerRepo: WorkerRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState
    
    init { search(null) }
    
    fun search(skillType: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val workers = if (skillType == null) workerRepo.getAllAvailableWorkers() else workerRepo.searchWorkers(skillType)
            _uiState.value = _uiState.value.copy(isLoading = false, workers = workers)
        }
    }
}
