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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SavingState {
    data object Loading : SavingState
    data class Review(
        val localId: Long,
        val title: String,
        val thumbnailPath: String?,
        val sourcePlatform: String,
        val authorUsername: String?,
    ) : SavingState
    data class Error(val localId: Long) : SavingState
    data class Saved(val title: String, val listNames: List<String>) : SavingState
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

    /** From the Error state: keep the row (bare link) and let the user fill in the review sheet themselves. */
    fun saveAnyway() {
        val localId = currentLocalId ?: return
        viewModelScope.launch { loadReview(localId, blankTitle = true) }
    }

    fun onTitleChange(newTitle: String) {
        (_state.value as? SavingState.Review)?.let { _state.value = it.copy(title = newTitle) }
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

    /** Saves the (possibly edited) title and list selection together, then shows the confirmation state. */
    fun saveRecipe() {
        val review = _state.value as? SavingState.Review ?: return
        val title = review.title.trim()
        if (title.isEmpty()) return
        viewModelScope.launch {
            repository.confirmName(review.localId, title)
            val selected = _selectedCollectionIds.value
            collectionRepository.setRecipeCollections(review.localId, selected)
            val listNames = collections.value.filter { it.id in selected }.map { it.name }
            _state.value = SavingState.Saved(title, listNames)
        }
    }

    fun discard(onDiscarded: () -> Unit) {
        val localId = currentLocalId ?: return
        viewModelScope.launch {
            repository.discard(localId)
            onDiscarded()
        }
    }

    private suspend fun applyOutcome(outcome: SubmitOutcome) {
        when (outcome) {
            is SubmitOutcome.Success -> {
                currentLocalId = outcome.localId
                loadReview(outcome.localId, blankTitle = false)
            }
            is SubmitOutcome.NeedsName -> {
                currentLocalId = outcome.localId
                loadReview(outcome.localId, blankTitle = true)
            }
            is SubmitOutcome.Failure -> {
                currentLocalId = outcome.localId
                _state.value = SavingState.Error(outcome.localId)
            }
        }
    }

    private suspend fun loadReview(localId: Long, blankTitle: Boolean) {
        val recipe = repository.observeRecipe(localId).first()
        _state.value = SavingState.Review(
            localId = localId,
            title = if (blankTitle) "" else recipe?.title.orEmpty(),
            thumbnailPath = recipe?.thumbnailPath,
            sourcePlatform = recipe?.sourcePlatform ?: "unknown",
            authorUsername = recipe?.authorUsername,
        )
    }
}
