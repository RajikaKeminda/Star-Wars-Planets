package com.example.starwarsplanets.data.models


data class PlanetResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<Planet>
)

data class Planet(
    val name: String,
    val climate: String,
    val gravity: String,
    val url: String,
    var imageUrl: String,
)
