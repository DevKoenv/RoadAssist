package dev.koenv.roadassist.app.data.incidents

import dev.koenv.roadassist.app.data.storage.AndroidContextHolder
import dev.koenv.roadassist.core.Incident
import java.io.File
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual fun createIncidentCache(): LocalIncidentCache {
    val filesDir = AndroidContextHolder.applicationContext.filesDir
    return AndroidIncidentCache(filesDir)
}

private val cacheJson = Json { ignoreUnknownKeys = true }

private class AndroidIncidentCache(private val filesDir: File) : LocalIncidentCache {

    private val cacheFile = File(filesDir, "incidents.json")

    override fun save(incidents: List<Incident>) {
        cacheFile.writeText(cacheJson.encodeToString(incidents))
    }

    @Suppress("TooGenericExceptionCaught")
    override fun load(): List<Incident> = runCatching {
        if (!cacheFile.exists()) return emptyList<Incident>()
        cacheJson.decodeFromString<List<Incident>>(cacheFile.readText())
    }.getOrElse { emptyList() }
}
