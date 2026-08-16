package com.kveld9.fcmetrix.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kveld9.fcmetrix.MainActivity
import com.kveld9.fcmetrix.R
import com.kveld9.fcmetrix.ui.theme.FcmTheme
import com.kveld9.fcmetrix.ui.viewmodel.GrlViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GrlScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun grlScreen_fullFlow_calculatesAndClears() {
        // 1. Verificar estado inicial usando Tag
        composeTestRule.onNodeWithTag("grl_missing_hint", useUnmergedTree = true).assertIsDisplayed()

        // 2. Llenar 11 titulares usando Tags y reemplazo rápido
        for (i in 1..11) {
            composeTestRule.onNodeWithTag("grl_input_$i", useUnmergedTree = true).performTextReplacement("100")
        }

        // 3. Verificar que el GRL Global aparezca (100) usando Tag
        composeTestRule.onNodeWithTag("grl_global_value", useUnmergedTree = true).assertTextEquals("100")

        // 4. Agregar suplente usando Tag
        composeTestRule.onNodeWithTag("btn_add_substitute", useUnmergedTree = true).performClick()
        
        // Verificamos que aparezca el nuevo campo y escribimos
        composeTestRule.onNodeWithTag("grl_input_12", useUnmergedTree = true).performTextReplacement("112")
        
        // 11*100 + 112 = 1212. n=12. avg=101.
        composeTestRule.onNodeWithTag("grl_global_value", useUnmergedTree = true).assertTextEquals("101")

        // 5. Limpiar todo usando Tag
        composeTestRule.onNodeWithTag("btn_clear_all", useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithTag("grl_missing_hint", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun grlScreen_partialFill_doesNotCalculateGlobalOvr() {
        val teamPlaceholder = composeTestRule.activity.getString(R.string.team)

        // Llenar solo 5 titulares usando Tags
        for (i in 1..5) {
            composeTestRule.onNodeWithTag("grl_input_$i", useUnmergedTree = true).performTextReplacement("100")
        }

        composeTestRule.onNodeWithTag("grl_missing_hint", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("grl_global_value", useUnmergedTree = true).assertTextEquals(teamPlaceholder)
    }
}
