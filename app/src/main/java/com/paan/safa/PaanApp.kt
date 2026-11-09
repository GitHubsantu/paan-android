package com.paan.safa

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.memory.MemoryCache
import coil3.request.crossfade
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PaanApp : Application(), SingletonImageLoader.Factory {

    /** Smaller in-memory image cache (15% of the app's memory) so low-RAM phones aren't squeezed. */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .memoryCache { MemoryCache.Builder().maxSizePercent(context, 0.15).build() }
            .crossfade(150)
            .build()

    override fun onCreate() {
        super.onCreate()
        com.paan.safa.push.NotificationChannels.create(this)
    }
}