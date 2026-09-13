package com.recipehub.app.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipehub.app.data.model.Collection
import com.recipehub.app.data.model.Recipe
import com.recipehub.app.data.repository.CollectionRepository
import com.recipehub.app.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val repository: RecipeRepository,
    private val collectionRepository: CollectionRepository,
) : ViewModel() {

    private val _selectedCollectionIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedCollectionIds: StateFlow<Set<Long>> = _selectedCollectionIds

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val collections: StateFlow<List<Collection>> = collectionRepository.observeCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recipes: StateFlow<List<Recipe>> = combine(
        _selectedCollectionIds.flatMapLatest { ids -> repository.observeRecipes(ids) },
        _searchQuery,
    ) { list, query ->
        if (query.isBlank()) list else list.filter { it.title.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleCollection(id: Long) {
        _selectedCollectionIds.value = _selectedCollectionIds.value.let {
            if (id in it) it - id else it + id
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun createCollection(name: String) {
        viewModelScope.launch { collectionRepository.createCollection(name) }
    }

    fun renameCollection(id: Long, name: String) {
        viewModelScope.launch { collectionRepository.renameCollection(id, name) }
    }

    fun deleteCollection(id: Long) {
        viewModelScope.launch {
            collectionRepository.deleteCollection(id)
            _selectedCollectionIds.value = _selectedCollectionIds.value - id
        }
    }

    fun deleteRecipe(localId: Long) {
        viewModelScope.launch { repository.deleteRecipe(localId) }
    }
}
