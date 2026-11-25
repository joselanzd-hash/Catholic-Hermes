package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.models.AppLanguage
import com.example.ui.screens.SaintsScreen
import com.example.ui.theme.SanctaFidesTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** L5: in landscape, selecting a saint changes the detail pane without pushing a page. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], qualifiers = "w891dp-h411dp-land-xhdpi")
class SaintsLandscapeL5Test {
    @get:Rule val compose = createComposeRule()

    @Test fun selectionStaysInTwoPanesAndBackLeavesTheScreen() {
        var exited = false
        compose.setContent {
            SanctaFidesTheme(designLanguage = "LUMEN", darkModeSetting = "LIGHT", reduceMotion = true) {
                SaintsScreen(AppLanguage.ES, initialKey = "basil_the_great_and_gregory_nazianzen_bishops", onExitContext = { exited = true })
            }
        }
        compose.waitForIdle()
        compose.onNodeWithTag("two_pane").assertIsDisplayed()
        compose.onNodeWithTag("list_filter_button").assertIsDisplayed()
        compose.onNodeWithTag("saint_detail").assertIsDisplayed()
        compose.onNodeWithTag("saint_row_most_holy_name_of_jesus").performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("two_pane").assertIsDisplayed()
        assert(!exited)
    }

    @Test
    @Config(sdk = [34], qualifiers = "w411dp-h891dp")
    fun portraitKeepsInlineFiltersAndDetailBack() {
        compose.setContent {
            SanctaFidesTheme(designLanguage = "LUMEN", darkModeSetting = "LIGHT", reduceMotion = true) {
                SaintsScreen(AppLanguage.ES, initialKey = "francis_de_sales_bishop")
            }
        }
        compose.waitForIdle()
        // r2: the detail opened from outside still exists in portrait and «back» returns to the list.
        compose.onNodeWithTag("saint_detail").assertExists()
        compose.onNodeWithTag("saint_detail_back").performClick()
        compose.onNodeWithTag("saints_screen").assertIsDisplayed()
        // Vertical keeps the original inline filters; only LandscapePhone moves them to a sheet.
        compose.onNodeWithTag("saints_month_picker").assertIsDisplayed()
        compose.onNodeWithTag("list_filter_button").assertDoesNotExist()
        compose.onNodeWithTag("saints_month_picker").performClick()
        compose.onNodeWithTag("saints_month_1").performClick()
        compose.onNodeWithTag("saint_row_agatha_of_sicily_virgin").assertDoesNotExist()
    }
}
