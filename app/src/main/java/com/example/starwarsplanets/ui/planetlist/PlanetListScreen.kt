package com.example.starwarsplanets.ui.planetlist

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.starwarsplanets.data.models.Planet
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanetListScreen(
    onPlanetClick: (Planet) -> Unit,
    viewModel: PlanetListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    val isAtEnd by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsCount = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            lastVisibleItemIndex > 0 && lastVisibleItemIndex >= totalItemsCount - 2
        }
    }

    LaunchedEffect(isAtEnd) {
        if (isAtEnd) {
            when (val state = uiState) {

                is PlanetListUiState.Success -> {
                    if (state.canLoadMore) {
                        viewModel.loadNextPage()
                    }
                }

                else -> {} // Handle other states
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Star Wars Planets") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is PlanetListUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is PlanetListUiState.Success -> {
                    val planets = (uiState as PlanetListUiState.Success).planets
                    PlanetList(
                        state = listState,
                        planets = planets,
                        onPlanetClick = onPlanetClick,
                        canLoadMore = (uiState as PlanetListUiState.Success).canLoadMore,
                    )
                }

                is PlanetListUiState.Error -> {
                    val errorMessage = (uiState as PlanetListUiState.Error).message
                    ErrorView(
                        message = errorMessage,
                        onRetry = { viewModel.loadPlanets() }
                    )
                }

                is PlanetListUiState.LoadingMore -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun PlanetList(
    state: LazyListState,
    planets: List<Planet>,
    onPlanetClick: (Planet) -> Unit,
    canLoadMore: Boolean,
) {
    LazyColumn(
        state = state,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = planets,
            key = { planet -> planet.url } // Use unique URL as key to prevent duplicates
        ) { planet ->
            PlanetCard(
                planet = planet,
                onClick = { onPlanetClick(planet) }
            )
        }

        if (canLoadMore) {
            item(key = "loading") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PlanetCard(
    planet: Planet,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(8.dp)
        ) {
            // Planet image
            AsyncImage(
                model = planet.imageUrl,
                contentDescription = "Planet image for ${planet.name}",
                modifier = Modifier
                    .size(104.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            // Planet info
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = planet.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Climate: ${planet.climate}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}