package dev.koenv.roadassist.app.data.incidents

import dev.koenv.roadassist.core.Incident
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual fun createIncidentCache(): LocalIncidentCache {
    val dir = System.getProperty("roadassist.storageDir")
        ?.let { Path.of(it) }
        ?: Path.of(System.getProperty("user.home"), ".roadassist")
    return JvmIncidentCache(dir)
}

private val cacheJson = Json { ignoreUnknownKeys = true }

private class JvmIncidentCache(private val storageDir: Path) : LocalIncidentCache {

    private val cacheFile = storageDir.resolve("incidents.json")

    override fun save(incidents: List<Incident>) {
        storageDir.createDirectories()
        cacheFile.writeText(cacheJson.encodeToString(incidents))
    }

    @Suppress("TooGenericExceptionCaught")
    override fun load(): List<Incident> = runCatching {
        if (!cacheFile.exists()) return emptyList<Incident>()
        cacheJson.decodeFromString<List<Incident>>(cacheFile.readText())
    }.getOrElse { emptyList() }
}
