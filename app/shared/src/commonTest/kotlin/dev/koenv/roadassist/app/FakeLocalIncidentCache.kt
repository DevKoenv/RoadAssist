package dev.koenv.roadassist.app

import dev.koenv.roadassist.app.data.incidents.LocalIncidentCache
import dev.koenv.roadassist.core.Incident

class FakeLocalIncidentCache(private val initial: List<Incident> = emptyList()) : LocalIncidentCache {
    private var stored: List<Incident> = initial
    override fun save(incidents: List<Incident>) { stored = incidents }
    override fun load(): List<Incident> = stored
}
