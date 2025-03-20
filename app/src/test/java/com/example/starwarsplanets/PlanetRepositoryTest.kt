package com.example.starwarsplanets

import com.example.starwarsplanets.data.api.SwapiService
import com.example.starwarsplanets.data.local.PlanetDao
import com.example.starwarsplanets.data.local.PlanetEntity
import com.example.starwarsplanets.data.models.Planet
import com.example.starwarsplanets.data.models.PlanetResponse
import com.example.starwarsplanets.data.repository.NetworkResult
import com.example.starwarsplanets.data.repository.PlanetRepository
import com.example.starwarsplanets.util.NetworkConnectivityUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class PlanetRepositoryTest {

    private lateinit var repository: PlanetRepository
    private val swapiService: SwapiService = Mockito.mock(SwapiService::class.java)
    private val planetDao: PlanetDao = Mockito.mock(PlanetDao::class.java)
    private val networkUtil: NetworkConnectivityUtil =
        Mockito.mock(NetworkConnectivityUtil::class.java)

    @Before
    fun setup() {
        repository = PlanetRepository(swapiService, planetDao)
    }

    @Test
    fun `getPlanets should return Success when online and API call is successful`() = runBlocking {
        val planets = listOf(
            Planet(
                name = "Tatooine",
                climate = "Arid",
                gravity = "1 standard",
                url = "https://swapi.dev/api/planets/1/",
                imageUrl = ""
            )
        )
        val planetResponse = PlanetResponse(
            count = 1,
            next = null,
            previous = null,
            results = planets
        )

        `when`(networkUtil.isNetworkAvailable()).thenReturn(true)
        `when`(swapiService.getPlanets(1)).thenReturn(planetResponse)

        val result = repository.getPlanets(1, true).drop(1).first()

        assertTrue("Expected Success result but got: $result", result is NetworkResult.Success)
        assertEquals(planets.size, (result as NetworkResult.Success).data.size)
        assertEquals(planets[0].name, (result as NetworkResult.Success).data[0].name)
    }

    @Test
    fun `getPlanets should return cached data when offline`() = runBlocking {

        fun getMockData(): Flow<List<PlanetEntity>>  {
            val planets = listOf(
                PlanetEntity(
                    name = "Tatooine",
                    climate = "Arid",
                    gravity = "1 standard",
                    url = "https://swapi.dev/api/planets/1/",
                    imageUrl = "https://example.com/image.jpg"
                )
            )
            return  flowOf(planets)
        }

        val planets = getMockData()
        `when`(networkUtil.isNetworkAvailable()).thenReturn(false)
        `when`(planetDao.getAllPlanets()).thenReturn(getMockData())

        val result = repository.getPlanets(1, false).drop(1).first()

        val planetList = planets.toList()[0]
        assertTrue("Expected Success result but got: $result", result is NetworkResult.Success)
        assertEquals(planetList.size, (result as NetworkResult.Success).data.size)
        assertEquals(planetList[0].name, (result as NetworkResult.Success).data[0].name)
    }


    @Test
    fun `getPlanets should cache data after successful API call`(): Unit = runBlocking {
        val planets = listOf(
            Planet(
                name = "Tatooine",
                climate = "Arid",
                gravity = "1 standard",
                url = "https://swapi.dev/api/planets/1/",
                imageUrl = ""
            )
        )
        val planetResponse = PlanetResponse(
            count = 1,
            next = null,
            previous = null,
            results = planets
        )

        `when`(networkUtil.isNetworkAvailable()).thenReturn(true)
        `when`(swapiService.getPlanets(1)).thenReturn(planetResponse)

        repository.getPlanets(1, true).first()

        verify(planetDao).insertPlanets(Mockito.anyList())
    }

    @Test
    fun `hasNextPage should return false when offline`() {
        val result = repository.hasNextPage()

        assertEquals(false, result)
    }
}