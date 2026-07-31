package com.ibrahim.metaremover.di

import com.ibrahim.metaremover.core.ai.CompositeAIDetector
import com.ibrahim.metaremover.core.ai.detectors.OpenAIDetector
import com.ibrahim.metaremover.core.parser.JPEGParser
import com.ibrahim.metaremover.core.parser.PNGParser
import com.ibrahim.metaremover.core.parser.ParserManager
import com.ibrahim.metaremover.data.repository.ImageRepositoryImpl
import com.ibrahim.metaremover.domain.repository.ImageRepository
import com.ibrahim.metaremover.domain.usecase.AnalyzeImageUseCase
import com.ibrahim.metaremover.domain.usecase.CleanImageUseCase
import com.ibrahim.metaremover.presentation.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val commonModule = module {
    // NOTE: do not register these as `single<List<MetadataParser>>` /
    // `single<List<AIProviderDetector>>`. On the JVM both erase to plain `List`,
    // so Koin keys them identically and the second definition silently overrides
    // the first — ParserManager would then receive the AI-detector list and crash
    // with a ClassCastException on the first image. Build each list inline instead.
    single { ParserManager(listOf(JPEGParser(), PNGParser())) }

    single { CompositeAIDetector(listOf(OpenAIDetector())) }
    
    single<ImageRepository> { ImageRepositoryImpl(get(), get(), get(), get()) }
    
    single { AnalyzeImageUseCase(get()) }
    single { CleanImageUseCase(get()) }
    single { com.ibrahim.metaremover.domain.usecase.CalculatePrivacyScoreUseCase() }
    
    viewModelOf(::MainViewModel)
}

fun initKoin(appModule: org.koin.core.module.Module) = org.koin.core.context.startKoin {
    modules(commonModule, appModule)
}
