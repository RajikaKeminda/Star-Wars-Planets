package com.example.starwarsplanets.data.repository

import com.example.starwarsplanets.data.api.SwapiService
import com.example.starwarsplanets.data.local.PlanetDao
import com.example.starwarsplanets.data.local.toDomain
import com.example.starwarsplanets.data.local.toEntity
import com.example.starwarsplanets.data.models.Planet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.random.Random

sealed class NetworkResult {
    class Success(val data: List<Planet>) : NetworkResult()
    class Error(val message: String) : NetworkResult()
    class Loading : NetworkResult()
}

class PlanetRepository @Inject constructor(
    private val swapiService: SwapiService,
    private val planetDao: PlanetDao
) {

    private var nextPageUrl: String? = null

    fun getPlanets(
        page: Int? = null,
        forceRefresh: Boolean = false
    ): Flow<NetworkResult> = flow<NetworkResult> {
        emit(NetworkResult.Loading())

        try {
            val response = swapiService.getPlanets(page)
            nextPageUrl = response.next

            val planets = response.results.map { planet ->
                val imageId =  Random.nextInt(1, 1000)
                planet.imageUrl = "https://picsum.photos/id/$imageId/200/200"
                planet
            }

            // Caching data
            val planetEntities = planets.map { value ->
                value.toEntity()
            }

            if (page == 1 || page == null) {
                planetDao.clearAllPlanets()
            }
            planetDao.insertPlanets(planetEntities)

            emit(NetworkResult.Success(planets))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: "Request Failed"))
        }

    }.flowOn(Dispatchers.IO)

    fun hasNextPage(): Boolean {
        return nextPageUrl != null
    }
}