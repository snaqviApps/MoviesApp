package edu.review.moviesappreview.presentation.viewmodel

import edu.review.moviesappreview.domain.PowerState
import edu.review.moviesappreview.domain.repository.PowerRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PowerViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: FakePowerRepository
    private lateinit var viewModel: PowerViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakePowerRepository()
        viewModel = PowerViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should match the initialValue in stateIn` () = runTest {
        val expected = PowerState.PluggedStatusLoading(true)
        assertEquals(expected, viewModel.powerState.value)
    }

    @Test
    fun `upon emitting pluggedIn, check powerState matches`() = runTest {

        val collectJob = launch(testDispatcher) {
            viewModel.powerState.collect {}
        }

        //2. Act: Emit a new value to the flow, from fake Repository
        val newStatus = PowerState.PluggedIn
        fakeRepository.emit(newStatus)

        //3. Assert: Verify the ViewModel reflects the new value
        assertEquals(
            newStatus,
            viewModel.powerState.value
        )
        collectJob.cancel()

    }
}

class FakePowerRepository : PowerRepository {
    private val flow = MutableStateFlow<PowerState>(
        PowerState.PluggedStatusLoading(true))

    override fun getPowerState(): Flow<PowerState> = flow

    suspend fun emit(value: PowerState) {
        flow.emit(value)
    }

}
