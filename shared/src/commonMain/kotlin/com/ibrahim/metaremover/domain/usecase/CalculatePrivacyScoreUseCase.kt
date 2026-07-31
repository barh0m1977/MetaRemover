package com.ibrahim.metaremover.domain.usecase

import com.ibrahim.metaremover.domain.PrivacyScoreCalculator
import com.ibrahim.metaremover.domain.ImageMetadata

class CalculatePrivacyScoreUseCase {
    operator fun invoke(meta: ImageMetadata): Int = PrivacyScoreCalculator.calculate(meta)
}
