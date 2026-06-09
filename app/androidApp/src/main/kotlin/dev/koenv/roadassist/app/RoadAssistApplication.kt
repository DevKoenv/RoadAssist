package dev.koenv.roadassist.app

import android.app.Application

class RoadAssistApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidContextHolder.applicationContext = applicationContext
    }
}
