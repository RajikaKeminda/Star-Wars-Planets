package com.example.starwarsplanets.data.api

import com.example.starwarsplanets.data.models.PlanetResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SwapiService {
    @GET("/planets")
    suspend fun getPlanets(@Query("page") page: Int? = null): PlanetResponse
}