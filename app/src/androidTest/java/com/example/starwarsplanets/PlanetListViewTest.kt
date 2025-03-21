package com.example.starwarsplanets

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.starwarsplanets.ui.planetlist.PlanetListScreen
import com.example.starwarsplanets.ui.theme.StarWarsPlanetsTheme
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class PlanetListViewTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()


    @Test
    fun myTest() {
        // Start the app
        composeTestRule.setContent {
            StarWarsPlanetsTheme {
                PlanetListScreen(
                    onPlanetClick = {},
                    viewModel = mock()
                )
            }
        }

        composeTestRule.onNodeWithText("Continue").assertDoesNotExist()
    }
}