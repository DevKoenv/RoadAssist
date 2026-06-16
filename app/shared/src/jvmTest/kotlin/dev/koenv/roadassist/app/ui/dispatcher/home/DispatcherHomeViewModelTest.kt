package dev.koenv.roadassist.app.ui.dispatcher.home

import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.koenv.roadassist.app.data.api.FakeApiClient
import dev.koenv.roadassist.app.data.incidents.IncidentRepository
import dev.koenv.roadassist.app.data.storage.createSecureStorage
import dev.koenv.roadassist.app.db.RoadAssistDb
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class DispatcherHomeViewModelTest {

    private var tempDir: File? = null
    private var vm: DispatcherHomeViewModel? = null

    @BeforeTest
    fun setup() {
        tempDir = Files.createTempDirectory("roadassist-dispatcher-vm-test").toFile()
        System.setProperty("roadassist.storageDir", tempDir!!.absolutePath)
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun cleanup() {
        vm?.viewModelScope?.cancel()
        vm = null
        Dispatchers.resetMain()
        System.clearProperty("roadassist.storageDir")
        tempDir?.deleteRecursively()
    }

    private fun makeVm(api: FakeApiClient = FakeApiClient()): DispatcherHomeViewModel {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        RoadAssistDb.Schema.create(driver)
        return DispatcherHomeViewModel(
            apiClient = api,
            storage = createSecureStorage(),
            repository = IncidentRepository(api, RoadAssistDb(driver)),
        )
    }

    @Test
    fun initial_incidents_state_is_empty() = runTest {
        vm = makeVm()
        try {
            assertTrue(vm!!.incidents.value.isEmpty())
        } finally {
            vm!!.viewModelScope.cancel()
        }
    }

    @Test
    fun server_reachable_defaults_to_true() = runTest {
        vm = makeVm()
        try {
            assertTrue(vm!!.serverReachable.value)
        } finally {
            vm!!.viewModelScope.cancel()
        }
    }

    @Test
    fun incidents_loading_is_false_initially() = runTest {
        vm = makeVm()
        try {
            assertFalse(vm!!.incidentsLoading.value)
        } finally {
            vm!!.viewModelScope.cancel()
        }
    }
}
