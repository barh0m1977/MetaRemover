package com.ibrahim.metaremover

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform