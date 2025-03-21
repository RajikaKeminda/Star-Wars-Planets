package com.example.starwarsplanets

import com.example.starwarsplanets.data.models.Planet
import com.example.starwarsplanets.data.repository.NetworkResult
import com.example.starwarsplanets.data.repository.PlanetRepository
import com.example.starwarsplanets.ui.planetlist.PlanetListUiState
import com.example.starwarsplanets.ui.planetlist.PlanetListViewModel
import com.example.starwarsplanets.util.NetworkConnectivityUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations


class PlanetListViewModelTest {

    @Mock
    private lateinit var repository: PlanetRepository

    @Mock
    private lateinit var networkConnectivityUtil: NetworkConnectivityUtil

    private lateinit var viewModel: PlanetListViewModel

    @ExperimentalCoroutinesApi
    private val testDispatcher = StandardTestDispatcher()

    private val testPlanets = listOf(
        Planet(
            name = "Tatooine",
            climate = "Arid",
            gravity = "1 standard",
            url = "https://swapi.dev/api/planets/1/",
            imageUrl = "https://example.com/tatooine.jpg"
        ),
        Planet(
            name = "Alderaan",
            climate = "Temperate",
            gravity = "1 standard",
            url = "https://swapi.dev/api/planets/2/",
            imageUrl = "https://example.com/alderaan.jpg"
        )
    )

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        `when`(networkConnectivityUtil.isNetworkAvailable()).thenReturn(true)

        `when`(repository.hasNextPage()).thenReturn(true)
    }

    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cancel()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when repository returns success, uiState is Success with planets`(): Unit =
        runTest {

            val testFlow = MutableStateFlow<NetworkResult>(NetworkResult.Loading())

            `when`(networkConnectivityUtil.isNetworkAvailable()).thenReturn(true)
            `when`(repository.getPlanets(1, true)).thenReturn(
                testFlow
            )

            viewModel = PlanetListViewModel(repository, networkConnectivityUtil)


            val currentState = viewModel.uiState.value
            assertTrue("Initial state should be Loading", currentState is PlanetListUiState.Loading)

            testFlow.emit(NetworkResult.Success(testPlanets))

            advanceUntilIdle()

            val successState = viewModel.uiState.value as PlanetListUiState.Success
            assertEquals(2, successState.planets.size)
            assertEquals("Tatooine", successState.planets[0].name)
            assertEquals("Alderaan", successState.planets[1].name)
            assertTrue(successState.canLoadMore)
        }

    @ExperimentalCoroutinesApi
    @Test
    fun `when repository returns error, uiState is Error`(): Unit = runTest {
        val errorMessage = "Network error"
        val testFlow = MutableStateFlow<NetworkResult>(NetworkResult.Loading())

        `when`(networkConnectivityUtil.isNetworkAvailable()).thenReturn(true)
        `when`(repository.getPlanets(1, true)).thenReturn(
            testFlow
        )

        viewModel = PlanetListViewModel(repository, networkConnectivityUtil)


        var currentState = viewModel.uiState.value
        assertTrue("Initial state should be Loading", currentState is PlanetListUiState.Loading)

        testFlow.emit(NetworkResult.Error(errorMessage))

        advanceUntilIdle()

        currentState = viewModel.uiState.value
        assertTrue(currentState is PlanetListUiState.Error)

        val errorState = currentState as PlanetListUiState.Error
        assertEquals(errorMessage, errorState.message)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when network unavailable, load from cache`(): Unit = runTest {

        val testFlow = MutableStateFlow<NetworkResult>(NetworkResult.Loading())
        `when`(networkConnectivityUtil.isNetworkAvailable()).thenReturn(false)
        `when`(repository.getPlanets(1, false)).thenReturn(
            testFlow
        )

        viewModel = PlanetListViewModel(repository, networkConnectivityUtil)

        advanceUntilIdle()

        verify(repository).getPlanets(1, false)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when loadNextPage called, repository gets next page`(): Unit = runTest {

        val testFlow = MutableStateFlow<NetworkResult>(NetworkResult.Loading())
        `when`(networkConnectivityUtil.isNetworkAvailable()).thenReturn(true)
        `when`(repository.getPlanets(anyInt(), anyBoolean())).thenReturn(
            testFlow
        )
        `when`(repository.hasNextPage()).thenReturn(true)

        viewModel = PlanetListViewModel(repository, networkConnectivityUtil)

        testFlow.emit(NetworkResult.Success(testPlanets))
        advanceUntilIdle()

        viewModel.loadNextPage()

        advanceUntilIdle()

        verify(repository).getPlanets(2, true)
    }
}