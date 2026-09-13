package com.recipehub.app.ui.saving

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipehub.app.data.model.Collection
import com.recipehub.app.data.repository.CollectionRepository
import com.recipehub.app.data.repository.RecipeRepository
import com.recipehub.app.data.repository.SubmitOutcome
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SavingState {
    data object Loading : SavingState
    data class NeedsName(val localId: Long) : SavingState
    data class PickingLists(val localId: Long) : SavingState
    data class Error(val localId: Long) : SavingState
    data class Done(val localId: Long) : SavingState
}

@HiltViewModel
class SavingViewModel @Inject constructor(
    private val repository: RecipeRepository,
    private val collectionRepository: CollectionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<SavingState>(SavingState.Loading)
    val state: StateFlow<SavingState> = _state.asStateFlow()

    val collections: StateFlow<List<Collection>> = collectionRepository.observeCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCollectionIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedCollectionIds: StateFlow<Set<Long>> = _selectedCollectionIds

    private var currentLocalId: Long? = null

    fun submit(url: String) {
        viewModelScope.launch {
            _state.value = SavingState.Loading
            applyOutcome(repository.submitUrl(url))
        }
    }

    fun retry() {
        val localId = currentLocalId ?: return
        viewModelScope.launch {
            _state.value = SavingState.Loading
            applyOutcome(repository.retry(localId))
        }
    }

    /** From the Error state: keep the row (bare link) and let the user name it instead of discarding it. */
    fun saveAnyway() {
        val localId = currentLocalId ?: return
        _state.value = SavingState.NeedsName(localId)
    }

    fun confirmName(name: String) {
        val localId = currentLocalId ?: return
        val title = name.trim()
        if (title.isEmpty()) return
        viewModelScope.launch {
            repository.confirmName(localId, title)
            _state.value = SavingState.PickingLists(localId)
        }
    }

    fun toggleCollection(id: Long) {
        _selectedCollectionIds.value = _selectedCollectionIds.value.let {
            if (id in it) it - id else it + id
        }
    }

    fun createCollection(name: String) {
        viewModelScope.launch {
            val id = collectionRepository.createCollection(name)
            _selectedCollectionIds.value = _selectedCollectionIds.value + id
        }
    }

    /** Finishes the save with whatever lists are currently selected (possibly none — "Skip"). */
    fun finalizeLists() {
        val localId = currentLocalId ?: return
        viewModelScope.launch {
            collectionRepository.setRecipeCollections(localId, _selectedCollectionIds.value)
            _state.value = SavingState.Done(localId)
        }
    }

    fun discard(onDiscarded: () -> Unit) {
        val localId = currentLocalId ?: return
        viewModelScope.launch {
            repository.discard(localId)
            onDiscarded()
        }
    }

    private fun applyOutcome(outcome: SubmitOutcome) {
        currentLocalId = when (outcome) {
            is SubmitOutcome.Success -> outcome.localId
            is SubmitOutcome.NeedsName -> outcome.localId
            is SubmitOutcome.Failure -> outcome.localId
        }
        _state.value = when (outcome) {
            is SubmitOutcome.Success -> SavingState.PickingLists(outcome.localId)
            is SubmitOutcome.NeedsName -> SavingState.NeedsName(outcome.localId)
            is SubmitOutcome.Failure -> SavingState.Error(outcome.localId)
        }
    }
}
