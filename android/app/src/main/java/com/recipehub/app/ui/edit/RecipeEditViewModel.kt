package com.recipehub.app.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipehub.app.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditFormState(
    val title: String = "",
    val ingredientsText: String = "",
    val stepsText: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
)

@HiltViewModel
class RecipeEditViewModel @Inject constructor(
    private val repository: RecipeRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val localId: Long = checkNotNull(savedStateHandle["localId"])

    private val _form = MutableStateFlow(EditFormState())
    val form: StateFlow<EditFormState> = _form.asStateFlow()

    init {
        viewModelScope.launch {
            val recipe = repository.observeRecipe(localId).first()
            _form.value = EditFormState(
                title = recipe?.title.orEmpty(),
                ingredientsText = recipe?.ingredients?.joinToString("\n").orEmpty(),
                stepsText = recipe?.steps?.joinToString("\n").orEmpty(),
                isLoading = false,
            )
        }
    }

    fun onTitleChange(value: String) {
        _form.value = _form.value.copy(title = value)
    }

    fun onIngredientsChange(value: String) {
        _form.value = _form.value.copy(ingredientsText = value)
    }

    fun onStepsChange(value: String) {
        _form.value = _form.value.copy(stepsText = value)
    }

    fun save() {
        val current = _form.value
        viewModelScope.launch {
            _form.value = current.copy(isSaving = true)
            repository.saveEdits(
                localId = localId,
                title = current.title,
                ingredients = linesOf(current.ingredientsText),
                steps = linesOf(current.stepsText),
            )
            _form.value = current.copy(isSaving = false, saved = true)
        }
    }

    private fun linesOf(text: String): List<String> =
        text.lines().map { it.trim() }.filter { it.isNotEmpty() }
}
