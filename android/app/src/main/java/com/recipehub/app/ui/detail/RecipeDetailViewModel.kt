package com.recipehub.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipehub.app.data.model.Collection
import com.recipehub.app.data.model.Recipe
import com.recipehub.app.data.repository.CollectionRepository
import com.recipehub.app.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    repository: RecipeRepository,
    private val collectionRepository: CollectionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val localId: Long = checkNotNull(savedStateHandle["localId"])

    val recipe: StateFlow<Recipe?> = repository.observeRecipe(localId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allCollections: StateFlow<List<Collection>> = collectionRepository.observeCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recipeCollectionIds: StateFlow<Set<Long>> = collectionRepository.observeCollectionIdsForRecipe(localId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun toggleCollection(id: Long) {
        viewModelScope.launch {
            val current = recipeCollectionIds.value
            val updated = if (id in current) current - id else current + id
            collectionRepository.setRecipeCollections(localId, updated)
        }
    }

    fun createCollection(name: String) {
        viewModelScope.launch {
            val id = collectionRepository.createCollection(name)
            collectionRepository.setRecipeCollections(localId, recipeCollectionIds.value + id)
        }
    }
}
