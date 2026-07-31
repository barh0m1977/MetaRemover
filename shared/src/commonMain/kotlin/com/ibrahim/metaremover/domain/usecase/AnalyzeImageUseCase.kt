package com.ibrahim.metaremover.domain.usecase

import com.ibrahim.metaremover.domain.model.ImageAnalysis
import com.ibrahim.metaremover.domain.repository.ImageRepository

class AnalyzeImageUseCase(private val repository: ImageRepository) {
    suspend operator fun invoke(bytes: ByteArray): ImageAnalysis = repository.analyze(bytes)
}
