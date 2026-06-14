package dev.koenv.roadassist.app

import dev.koenv.roadassist.app.data.incidents.IncidentRepository
import dev.koenv.roadassist.app.geocoding.GeocodingResult
import dev.koenv.roadassist.app.ui.newincident.NewIncidentViewModel
import dev.koenv.roadassist.app.ui.newincident.SubmitState
import dev.koenv.roadassist.core.Incident
import dev.koenv.roadassist.core.IncidentCategory
import dev.koenv.roadassist.core.IncidentStatus
import dev.koenv.roadassist.core.LatLon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class NewIncidentViewModelTest {

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun cleanup() {
        Dispatchers.resetMain()
    }

    private fun sampleIncident() = Incident(
        id = 1, userId = 1, category = IncidentCategory.BREAKDOWN, description = "test",
        latitude = 51.0, longitude = 4.0, photoUrl = null, status = IncidentStatus.NEW,
        notes = null, createdAt = "now", updatedAt = "now",
    )

    @Test
    fun initial_category_is_breakdown() = runTest {
        val vm = NewIncidentViewModel(
            IncidentRepository(FakeApiClient(), FakeLocalIncidentCache()),
            FakeLocationProvider(LatLon(51.0, 4.0)),
            FakeMediaPicker(),
            FakeGeocodingService(),
        )
        assertEquals(IncidentCategory.BREAKDOWN, vm.category.value)
    }

    @Test
    fun location_populated_from_provider_after_init() = runTest {
        val vm = NewIncidentViewModel(
            IncidentRepository(FakeApiClient(), FakeLocalIncidentCache()),
            FakeLocationProvider(LatLon(52.0, 4.5)),
            FakeMediaPicker(),
            FakeGeocodingService(),
        )
        assertEquals(LatLon(52.0, 4.5), vm.location.value)
    }

    @Test
    fun location_is_null_when_provider_returns_null() = runTest {
        val vm = NewIncidentViewModel(
            IncidentRepository(FakeApiClient(), FakeLocalIncidentCache()),
            FakeLocationProvider(null),
            FakeMediaPicker(),
            FakeGeocodingService(),
        )
        assertNull(vm.location.value)
    }

    @Test
    fun submit_without_location_emits_error() = runTest {
        val vm = NewIncidentViewModel(
            IncidentRepository(FakeApiClient(), FakeLocalIncidentCache()),
            FakeLocationProvider(null),
            FakeMediaPicker(),
            FakeGeocodingService(),
        )
        vm.updateDescription("breakdown on A10")
        vm.submit()
        assertIs<SubmitState.Error>(vm.submitState.value)
    }

    @Test
    fun submit_with_blank_description_emits_error() = runTest {
        val vm = NewIncidentViewModel(
            IncidentRepository(FakeApiClient(), FakeLocalIncidentCache()),
            FakeLocationProvider(LatLon(51.0, 4.0)),
            FakeMediaPicker(),
            FakeGeocodingService(),
        )
        vm.submit()
        assertIs<SubmitState.Error>(vm.submitState.value)
    }

    @Test
    fun submit_success_emits_success_state() = runTest {
        val incident = sampleIncident()
        val fakeApi = FakeApiClient(createIncidentResult = Result.success(incident))
        val vm = NewIncidentViewModel(
            IncidentRepository(fakeApi, FakeLocalIncidentCache()),
            FakeLocationProvider(LatLon(51.0, 4.0)),
            FakeMediaPicker(),
            FakeGeocodingService(),
        )
        vm.updateDescription("engine failure")
        vm.submit()
        assertIs<SubmitState.Success>(vm.submitState.value)
    }

    @Test
    fun submit_api_failure_emits_error_state() = runTest {
        val fakeApi = FakeApiClient(
            createIncidentResult = Result.failure(
                dev.koenv.roadassist.app.data.api.ApiException.Network(RuntimeException("offline"))
            )
        )
        val vm = NewIncidentViewModel(
            IncidentRepository(fakeApi, FakeLocalIncidentCache()),
            FakeLocationProvider(LatLon(51.0, 4.0)),
            FakeMediaPicker(),
            FakeGeocodingService(),
        )
        vm.updateDescription("car broken")
        vm.submit()
        assertIs<SubmitState.Error>(vm.submitState.value)
    }

    @Test
    fun description_capped_at_500_chars() = runTest {
        val vm = NewIncidentViewModel(
            IncidentRepository(FakeApiClient(), FakeLocalIncidentCache()),
            FakeLocationProvider(LatLon(51.0, 4.0)),
            FakeMediaPicker(),
            FakeGeocodingService(),
        )
        val overlong = "a".repeat(600)
        vm.updateDescription(overlong)
        assertEquals(500, vm.description.value.length)
    }

    @Test
    fun locationLabel_populated_from_reverse_geocode() = runTest {
        val vm = NewIncidentViewModel(
            IncidentRepository(FakeApiClient(), FakeLocalIncidentCache()),
            FakeLocationProvider(LatLon(52.0, 4.5)),
            FakeMediaPicker(),
            FakeGeocodingService(reverseResult = "Amsterdam, Netherlands"),
        )
        assertEquals("Amsterdam, Netherlands", vm.locationLabel.value)
    }

    @Test
    fun locationLabel_null_when_provider_returns_null() = runTest {
        val vm = NewIncidentViewModel(
            IncidentRepository(FakeApiClient(), FakeLocalIncidentCache()),
            FakeLocationProvider(null),
            FakeMediaPicker(),
            FakeGeocodingService(reverseResult = "Should not be called"),
        )
        assertNull(vm.locationLabel.value)
    }

    @Test
    fun locationLabel_null_when_reverse_returns_null() = runTest {
        val vm = NewIncidentViewModel(
            IncidentRepository(FakeApiClient(), FakeLocalIncidentCache()),
            FakeLocationProvider(LatLon(52.0, 4.5)),
            FakeMediaPicker(),
            FakeGeocodingService(reverseResult = null),
        )
        assertNull(vm.locationLabel.value)
    }

    @Test
    fun setManualLocation_updates_location_and_label() = runTest {
        val vm = NewIncidentViewModel(
            IncidentRepository(FakeApiClient(), FakeLocalIncidentCache()),
            FakeLocationProvider(null),
            FakeMediaPicker(),
            FakeGeocodingService(),
        )
        vm.setManualLocation(51.9, 4.5, "Rotterdam, Netherlands")
        assertEquals(LatLon(51.9, 4.5), vm.location.value)
        assertEquals("Rotterdam, Netherlands", vm.locationLabel.value)
    }

    @Test
    fun background_gps_does_not_override_manual_location() = runTest {
        // GPS provides a location but user already picked one manually before it resolves.
        val vm = NewIncidentViewModel(
            IncidentRepository(FakeApiClient(), FakeLocalIncidentCache()),
            FakeLocationProvider(LatLon(52.0, 4.5)),
            FakeMediaPicker(),
            FakeGeocodingService(reverseResult = "Amsterdam, Netherlands"),
        )
        vm.setManualLocation(51.9, 4.5, "Rotterdam, Netherlands")
        // GPS result should be ignored because location was already set manually.
        assertEquals(LatLon(51.9, 4.5), vm.location.value)
        assertEquals("Rotterdam, Netherlands", vm.locationLabel.value)
    }
}
