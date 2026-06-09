package dev.koenv.roadassist.app

actual val BASE_URL: String
    get() {
        val ctx = AndroidContextHolder.applicationContext
        val resId = ctx.resources.getIdentifier("server_url", "string", ctx.packageName)
        return if (resId != 0) ctx.getString(resId) else "http://10.0.2.2:8080"
    }
