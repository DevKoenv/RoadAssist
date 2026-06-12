package dev.koenv.roadassist.app

import dev.koenv.roadassist.app.data.incidents.IncidentRepository
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
        val vm = dev.koenv.roadassist.app.ui.newincident.NewIncidentViewModel(
            IncidentRepository(FakeApiClient()),
            FakeLocationProvider(LatLon(51.0, 4.0)),
            FakeMediaPicker(),
        )
        assertEquals(IncidentCategory.BREAKDOWN, vm.category.value)
    }

    @Test
    fun location_populated_from_provider_after_init() = runTest {
        val vm = dev.koenv.roadassist.app.ui.newincident.NewIncidentViewModel(
            IncidentRepository(FakeApiClient()),
            FakeLocationProvider(LatLon(52.0, 4.5)),
            FakeMediaPicker(),
        )
        assertEquals(LatLon(52.0, 4.5), vm.location.value)
    }

    @Test
    fun location_is_null_when_provider_returns_null() = runTest {
        val vm = dev.koenv.roadassist.app.ui.newincident.NewIncidentViewModel(
            IncidentRepository(FakeApiClient()),
            FakeLocationProvider(null),
            FakeMediaPicker(),
        )
        assertNull(vm.location.value)
    }

    @Test
    fun submit_without_location_emits_error() = runTest {
        val vm = dev.koenv.roadassist.app.ui.newincident.NewIncidentViewModel(
            IncidentRepository(FakeApiClient()),
            FakeLocationProvider(null),
            FakeMediaPicker(),
        )
        vm.updateDescription("breakdown on A10")
        vm.submit()
        assertIs<SubmitState.Error>(vm.submitState.value)
    }

    @Test
    fun submit_with_blank_description_emits_error() = runTest {
        val vm = dev.koenv.roadassist.app.ui.newincident.NewIncidentViewModel(
            IncidentRepository(FakeApiClient()),
            FakeLocationProvider(LatLon(51.0, 4.0)),
            FakeMediaPicker(),
        )
        vm.submit()
        assertIs<SubmitState.Error>(vm.submitState.value)
    }

    @Test
    fun submit_success_emits_success_state() = runTest {
        val incident = sampleIncident()
        val fakeApi = FakeApiClient(createIncidentResult = Result.success(incident))
        val vm = dev.koenv.roadassist.app.ui.newincident.NewIncidentViewModel(
            IncidentRepository(fakeApi),
            FakeLocationProvider(LatLon(51.0, 4.0)),
            FakeMediaPicker(),
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
        val vm = dev.koenv.roadassist.app.ui.newincident.NewIncidentViewModel(
            IncidentRepository(fakeApi),
            FakeLocationProvider(LatLon(51.0, 4.0)),
            FakeMediaPicker(),
        )
        vm.updateDescription("car broken")
        vm.submit()
        assertIs<SubmitState.Error>(vm.submitState.value)
    }

    @Test
    fun description_capped_at_500_chars() = runTest {
        val vm = dev.koenv.roadassist.app.ui.newincident.NewIncidentViewModel(
            IncidentRepository(FakeApiClient()),
            FakeLocationProvider(LatLon(51.0, 4.0)),
            FakeMediaPicker(),
        )
        val overlong = "a".repeat(600)
        vm.updateDescription(overlong)
        assertEquals(500, vm.description.value.length)
    }
}
