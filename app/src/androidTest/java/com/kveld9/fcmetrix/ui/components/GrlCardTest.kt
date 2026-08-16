package com.kveld9.fcmetrix.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kveld9.fcmetrix.MainActivity
import com.kveld9.fcmetrix.R
import com.kveld9.fcmetrix.domain.GrlCalculator
import com.kveld9.fcmetrix.ui.theme.FcmTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GrlCardTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun grlCard_validResult_displaysGlobalOvr() {
        val globalOvrText = composeTestRule.activity.getString(R.string.grl_global)
        val result = GrlCalculator.Result(
            grlGlobal = 126,
            titularesCargados = 11,
            faltantes = 0,
            puntosGrl = 4,
            puntosRango = 2,
            esMejoraPorRango = false,
            rangoMaximo = false,
            promedioBase = 120.73,
            promedioRango = 4.10,
        )

        composeTestRule.setContent {
            FcmTheme {
                GrlCard(result = result)
            }
        }

        // Usamos useUnmergedTree = true porque los textos estan semánticamente ocultos del árbol fusionado
        composeTestRule.onNodeWithTag("grl_global_value", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(globalOvrText, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun grlCard_incompleteLineup_displaysMissingPlayersMessage() {
        val result = GrlCalculator.Result(
            grlGlobal = null,
            titularesCargados = 5,
            faltantes = 6,
            puntosGrl = null,
            puntosRango = null
        )

        composeTestRule.setContent {
            FcmTheme {
                GrlCard(result = result)
            }
        }

        composeTestRule.onNodeWithTag("grl_missing_hint", useUnmergedTree = true).assertIsDisplayed()
        val teamPlaceholder = composeTestRule.activity.getString(R.string.team)
        composeTestRule.onNodeWithTag("grl_global_value", useUnmergedTree = true)
            .assertTextEquals(teamPlaceholder)
    }

    @Test
    fun grlCard_maxRank_displaysMaxLabel() {
        val result = GrlCalculator.Result(
            grlGlobal = 130,
            titularesCargados = 11,
            faltantes = 0,
            puntosGrl = 1,
            puntosRango = null,
            esMejoraPorRango = false,
            rangoMaximo = true,
            promedioBase = 125.0,
            promedioRango = 5.0
        )

        composeTestRule.setContent {
            FcmTheme {
                GrlCard(result = result)
            }
        }

        composeTestRule.onNodeWithText("MAX", useUnmergedTree = true).assertIsDisplayed()
    }
}
