package com.ibrahim.metaremover.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibrahim.metaremover.domain.ImageGallerySaver
import com.ibrahim.metaremover.domain.usecase.AnalyzeImageUseCase
import com.ibrahim.metaremover.domain.usecase.CleanImageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

class MainViewModel(
    private val analyzeImage: AnalyzeImageUseCase,
    private val cleanImage: CleanImageUseCase,
    private val saver: ImageGallerySaver
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    fun onImagePicked(bytes: ByteArray?) {
        if (bytes == null) {
            _state.update { it.copy(error = "Selection cancelled") }
            return
        }

        viewModelScope.launch {
            // Surface the photo immediately so the Guardian Scan overlay has a
            // subject to visibly inspect while analysis runs.
            _state.update {
                it.copy(
                    originalBytes = bytes,
                    analysis = null,
                    originalMetadata = null,
                    cleanedBytes = null,
                    cleanedMetadata = null,
                    isProcessing = true,
                    progressPhase = ProgressPhase.ANALYZING,
                    loadingMessage = "Analyzing image...",
                    error = null
                )
            }
            try {
                val analysis = analyzeImage(bytes)
                _state.update {
                    it.copy(
                        analysis = analysis,
                        error = null,
                        isProcessing = false,
                        progressPhase = null,
                        loadingMessage = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Failed to analyze image: ${e.message}",
                        isProcessing = false,
                        progressPhase = null,
                        loadingMessage = null
                    )
                }
            }
        }
    }

    fun onCleanImage() {
        val bytes = _state.value.originalBytes ?: return
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isProcessing = true,
                    progressPhase = ProgressPhase.STRIPPING,
                    loadingMessage = "Stripping all metadata..."
                )
            }
            try {
                val result = cleanImage(bytes)
                val analysis = analyzeImage(result.cleanedBytes)
                _state.update {
                    it.copy(
                        cleanedBytes = result.cleanedBytes,
                        analysis = analysis,
                        isProcessing = false,
                        progressPhase = null,
                        loadingMessage = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Failed to clean image: ${e.message}",
                        isProcessing = false,
                        progressPhase = null,
                        loadingMessage = null
                    )
                }
            }
        }
    }

    fun onSaveImage() {
        val bytes = _state.value.cleanedBytes ?: return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isSaving = true,
                    progressPhase = ProgressPhase.SAVING,
                    loadingMessage = "Saving to gallery..."
                )
            }
            try {
                val timestamp = Clock.System.now().toEpochMilliseconds()
                val success = saver.saveImage(bytes, "cleaned_$timestamp.png")
                if (!success) {
                    _state.update { it.copy(error = "Failed to save image") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error saving image: ${e.message}") }
            } finally {
                _state.update { it.copy(isSaving = false, progressPhase = null, loadingMessage = null) }
            }
        }
    }
}
