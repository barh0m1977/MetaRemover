package com.ibrahim.metaremover.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibrahim.metaremover.data.ImageRepository
import com.ibrahim.metaremover.domain.ImageGallerySaver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

class MainViewModel(
    private val repository: ImageRepository,
    private val saver: ImageGallerySaver
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    fun onImagePicked(bytes: ByteArray?) {
        if (bytes == null) return

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, loadingMessage = "Analyzing image...") }
            try {
                val metadata = repository.analyze(bytes)
                _state.update {
                    it.copy(
                        originalBytes = bytes,
                        originalMetadata = metadata,
                        cleanedBytes = null,
                        cleanedMetadata = null,
                        error = null,
                        isProcessing = false,
                        loadingMessage = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Failed to analyze image: ${e.message}",
                        isProcessing = false,
                        loadingMessage = null
                    )
                }
            }
        }
    }

    fun onCleanImage() {
        val bytes = _state.value.originalBytes ?: return

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, loadingMessage = "Stripping all metadata...") }
            try {
                val cleaned = repository.clean(bytes)
                val metadata = repository.analyze(cleaned)
                _state.update {
                    it.copy(
                        cleanedBytes = cleaned,
                        cleanedMetadata = metadata,
                        isProcessing = false,
                        loadingMessage = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Failed to clean image: ${e.message}",
                        isProcessing = false,
                        loadingMessage = null
                    )
                }
            }
        }
    }

    fun onSaveImage() {
        val bytes = _state.value.cleanedBytes ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, loadingMessage = "Saving to gallery...") }
            try {
                val timestamp = Clock.System.now().toEpochMilliseconds()
                val success = saver.saveImage(bytes, "cleaned_$timestamp.png")
                if (!success) {
                    _state.update { it.copy(error = "Failed to save image") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error saving image: ${e.message}") }
            } finally {
                _state.update { it.copy(isSaving = false, loadingMessage = null) }
            }
        }
    }
}
