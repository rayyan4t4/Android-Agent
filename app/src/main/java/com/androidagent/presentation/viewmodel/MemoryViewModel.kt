package com.androidagent.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidagent.domain.model.MemoryEntry
import com.androidagent.domain.model.MemoryType
import com.androidagent.domain.usecase.GetMemoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val getMemory: GetMemoryUseCase
) : ViewModel() {

    private val _selectedType = MutableStateFlow(MemoryType.SHORT_TERM)
    val selectedType: StateFlow<MemoryType> = _selectedType

    val memories: StateFlow<List<MemoryEntry>> = _selectedType
        .flatMapLatest { type -> getMemory.observeAll(type) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun selectType(type: MemoryType) {
        _selectedType.value = type
    }

    fun clearMemory() {
        viewModelScope.launch { getMemory.clear(_selectedType.value) }
    }
}
