package dev.koenv.roadassist.app

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory

fun initCoil() {
    // setSafe is a no-op if Coil is already initialized, safe to call from multiple entry points
    SingletonImageLoader.setSafe { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }
}
