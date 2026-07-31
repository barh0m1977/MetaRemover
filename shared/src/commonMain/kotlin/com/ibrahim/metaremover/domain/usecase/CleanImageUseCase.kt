package com.ibrahim.metaremover.domain.usecase

import com.ibrahim.metaremover.domain.CleanResult
import com.ibrahim.metaremover.domain.repository.ImageRepository

class CleanImageUseCase(private val repository: ImageRepository) {
    suspend operator fun invoke(bytes: ByteArray): CleanResult = repository.clean(bytes)
}
