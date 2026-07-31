package com.ibrahim.metaremover.domain

interface MetadataCleaner {
    suspend fun clean(bytes: ByteArray): CleanResult
}
