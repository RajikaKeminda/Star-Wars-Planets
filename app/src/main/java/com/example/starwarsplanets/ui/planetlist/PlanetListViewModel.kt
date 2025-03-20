package com.example.starwarsplanets.ui.planetlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starwarsplanets.data.models.Planet
import com.example.starwarsplanets.data.repository.NetworkResult
import com.example.starwarsplanets.data.repository.PlanetRepository
import com.example.starwarsplanets.util.NetworkConnectivityUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlanetListViewModel @Inject constructor(
    private val repository: PlanetRepository,
    private val networkConnectivityUtil: NetworkConnectivityUtil
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlanetListUiState>(PlanetListUiState.Loading)
    val uiState: StateFlow<PlanetListUiState> = _uiState

    private var currentPage = 1
    private var isLoadingMore = false
    private val planetsList = mutableListOf<Planet>()

    init {
        if(networkConnectivityUtil.isNetworkAvailable()) {
            loadPlanets(forceRefresh = true)
        } else {
            loadPlanets(forceRefresh = false)
        }

    }

    fun loadPlanets(forceRefresh: Boolean = true) {
        if (isLoadingMore) return

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
                    when (result) {
                        is NetworkResult.Success -> {
                            isLoadingMore = false
                            val newPlanets = result.data
                            if (currentPage == 1) {
                                planetsList.clear()
                            }
                            planetsList.addAll(newPlanets)

                            _uiState.value = PlanetListUiState.Success(
                                planets = planetsList,
                                canLoadMore = if (forceRefresh) repository.hasNextPage() else false
                            )
                        }

                        is NetworkResult.Error -> {
                            isLoadingMore = false
                            if (planetsList.isEmpty()) {
                                _uiState.value =
                                    PlanetListUiState.Error(result.message ?: "Unknown error")
                            } else {
                                _uiState.value = PlanetListUiState.Success(
                                    planets = planetsList,
                                    canLoadMore = repository.hasNextPage()
                                )
                            }
                        }

                        is NetworkResult.Loading -> {
                            isLoadingMore = true
                            _uiState.value = PlanetListUiState.LoadingMore(planetsList)
                        }
                    }
                }
        }
    }

    fun loadNextPage() {
        if (!isLoadingMore && repository.hasNextPage()) {
            currentPage++
            loadPlanets(forceRefresh = true)
        }
    }

    fun refresh() {
        loadPlanets(forceRefresh = true)
    }
}


class PaginatedState {
    val items: List<Planet> = emptyList()
    val isLoading: Boolean = false
}

sealed class PlanetListUiState {
    object Loading : PlanetListUiState()
    data class LoadingMore(val planets: List<Planet>) : PlanetListUiState()
    data class Success(val planets: List<Planet>, val canLoadMore: Boolean = false) :
        PlanetListUiState()

    data class Error(val message: String) : PlanetListUiState()
}