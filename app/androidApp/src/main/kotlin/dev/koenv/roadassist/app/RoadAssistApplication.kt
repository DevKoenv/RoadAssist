package dev.koenv.roadassist.app

import android.app.Application
import dev.koenv.roadassist.app.data.storage.AndroidContextHolder

class RoadAssistApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidContextHolder.applicationContext = applicationContext
        initCoil()
    }
}
