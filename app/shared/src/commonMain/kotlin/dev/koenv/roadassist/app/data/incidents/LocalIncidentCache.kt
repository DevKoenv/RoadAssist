package dev.koenv.roadassist.app.data.incidents

import dev.koenv.roadassist.core.Incident

interface LocalIncidentCache {
    fun save(incidents: List<Incident>)
    fun load(): List<Incident>
}

expect fun createIncidentCache(): LocalIncidentCache
