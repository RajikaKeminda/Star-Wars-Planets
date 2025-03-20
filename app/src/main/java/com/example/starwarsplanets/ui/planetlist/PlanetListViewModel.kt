package com.example.starwarsplanets.ui.planetlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starwarsplanets.data.models.Planet
import com.example.starwarsplanets.data.repository.NetworkResult
import com.example.starwarsplanets.data.repository.PlanetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlanetListViewModel @Inject constructor(
    private val repository: PlanetRepository
): ViewModel() {

    private val _uiState = MutableStateFlow<PlanetListUiState>(PlanetListUiState.Loading)
    val uiState: StateFlow<PlanetListUiState> = _uiState

    private var currentPage = 1
    private var isLoadingMore = false
    private val planetsList = mutableListOf<Planet>()

    init {
        loadPlanets(forceRefresh = true)
    }

    fun loadPlanets(forceRefresh: Boolean = false) {
        if (isLoadingMore) return

        if (forceRefresh) {
            currentPage = 1
            planetsList.clear()
            _uiState.value = PlanetListUiState.Loading
        } else {
            _uiState.value = PlanetListUiState.LoadingMore(planetsList)
        }

        isLoadingMore = true

        viewModelScope.launch {
            repository.getPlanets(currentPage, forceRefresh)
                .catch { e ->
                    isLoadingMore = false
                    if (planetsList.isEmpty()) {
                        _uiState.value = PlanetListUiState.Error(e.message ?: "Unknown error")
                    } else {
                        _uiState.value = PlanetListUiState.Success(
                            planets = planetsList,
                            canLoadMore = repository.hasNextPage()
                        )
                    }
                }
                .collect { result ->
                    isLoadingMore = false
                    when (result) {
                        is NetworkResult.Success -> {
                            val newPlanets = result.data
                            if (currentPage == 1) {
                                planetsList.clear()
                            }
                            planetsList.addAll(newPlanets)

                            _uiState.value = PlanetListUiState.Success(
                                planets = planetsList,
                                canLoadMore = repository.hasNextPage()
                            )
                        }
                        is NetworkResult.Error -> {
                            if (planetsList.isEmpty()) {
                                _uiState.value = PlanetListUiState.Error(result.message ?: "Unknown error")
                            } else {
                                _uiState.value = PlanetListUiState.Success(
                                    planets = planetsList,
                                    canLoadMore = repository.hasNextPage()
                                )
                            }
                        }

                        is NetworkResult.Loading -> {
                            println("Loading ================")
                        }
                    }
                }
        }
    }

    fun loadNextPage() {
        if (!isLoadingMore && repository.hasNextPage()) {
            currentPage++
            loadPlanets(forceRefresh = false)
        }
    }

    fun refresh() {
        loadPlanets(forceRefresh = true)
    }
}

sealed class PlanetListUiState {
    object Loading : PlanetListUiState()
    data class LoadingMore(val planets: List<Planet>) : PlanetListUiState()
    data class Success(val planets: List<Planet>, val canLoadMore: Boolean = false) : PlanetListUiState()
    data class Error(val message: String) : PlanetListUiState()
}