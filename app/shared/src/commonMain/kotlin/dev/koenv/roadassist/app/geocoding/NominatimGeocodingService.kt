package dev.koenv.roadassist.app.geocoding

import dev.koenv.roadassist.app.network.createHttpClient
import dev.koenv.roadassist.core.LatLon
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val BASE_URL = "https://nominatim.openstreetmap.org"
private const val USER_AGENT = "RoadAssist/1.0.0 (Koenv.DEV)"

@Serializable
private data class NominatimPlace(
    @SerialName("display_name") val displayName: String,
    val lat: String,
    val lon: String,
)

class NominatimGeocodingService : GeocodingService {

    private val client = createHttpClient().config {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override suspend fun search(query: String): List<GeocodingResult> = try {
        client.get("$BASE_URL/search") {
            parameter("q", query)
            parameter("format", "json")
            parameter("limit", "5")
            header(HttpHeaders.UserAgent, USER_AGENT)
        }.body<List<NominatimPlace>>().map {
            GeocodingResult(
                label = it.displayName,
                location = LatLon(it.lat.toDouble(), it.lon.toDouble()),
            )
        }
    } catch (e: Exception) {
        emptyList()
    }

    override suspend fun reverse(location: LatLon): String? = try {
        client.get("$BASE_URL/reverse") {
            parameter("lat", location.latitude)
            parameter("lon", location.longitude)
            parameter("format", "json")
            header(HttpHeaders.UserAgent, USER_AGENT)
        }.body<NominatimPlace>().displayName
    } catch (e: Exception) {
        null
    }
}
