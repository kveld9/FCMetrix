package com.kveld9.fcmetrix.ui.components

import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kveld9.fcmetrix.R
import com.kveld9.fcmetrix.ui.theme.FcmTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun playerRow_inputGrl_callsOnGrlChange() {
        var grlValue = ""
        composeTestRule.setContent {
            FcmTheme {
                PlayerRow(
                    numero = 1,
                    grl = grlValue,
                    rango = "0",
                    onGrlChange = { grlValue = it },
                    onRangoChange = {},
                    onGrlFocusLost = {},
                    onRangoFocusLost = {},
                    scrollState = rememberScrollState(),
                )
            }
        }

        // Usamos useUnmergedTree = true para encontrar el tag dentro de la semántica agrupada
        composeTestRule.onNodeWithTag("grl_input_1", useUnmergedTree = true).performTextReplacement("120")
        assertEquals("120", grlValue)
    }

    @Test
    fun playerRow_inputRank_callsOnRangoChange() {
        var rangoValue = "0"
        composeTestRule.setContent {
            FcmTheme {
                PlayerRow(
                    numero = 1,
                    grl = "100",
                    rango = rangoValue,
                    onGrlChange = {},
                    onRangoChange = { rangoValue = it },
                    onGrlFocusLost = {},
                    onRangoFocusLost = {},
                    scrollState = rememberScrollState(),
                )
            }
        }

        composeTestRule.onNodeWithTag("rank_input_1", useUnmergedTree = true).performTextReplacement("5")
        assertEquals("5", rangoValue)
    }

    @Test
    fun playerRow_emptyInput_isAllowedDuringEditing() {
        var grlValue = "100"
        composeTestRule.setContent {
            FcmTheme {
                PlayerRow(
                    numero = 1,
                    grl = grlValue,
                    rango = "0",
                    onGrlChange = { grlValue = it },
                    onRangoChange = {},
                    onGrlFocusLost = {},
                    onRangoFocusLost = {},
                    scrollState = rememberScrollState(),
                )
            }
        }

        composeTestRule.onNodeWithTag("grl_input_1", useUnmergedTree = true).performTextReplacement("")
        assertEquals("", grlValue)
    }

    @Test
    fun playerRow_negativeInput_isNotConvertedToOne() {
        var grlValue = ""
        composeTestRule.setContent {
            FcmTheme {
                PlayerRow(
                    numero = 1,
                    grl = grlValue,
                    rango = "0",
                    onGrlChange = { grlValue = it },
                    onRangoChange = {},
                    onGrlFocusLost = {},
                    onRangoFocusLost = {},
                    scrollState = rememberScrollState(),
                )
            }
        }

        composeTestRule.onNodeWithTag("grl_input_1", useUnmergedTree = true).performTextReplacement("-1")
        assertEquals("-1", grlValue)
    }

    @Test
    fun playerRow_focusLost_triggersSanitizationCallback() {
        var sanitizedGrl = ""
        composeTestRule.setContent {
            FcmTheme {
                PlayerRow(
                    numero = 1,
                    grl = "151",
                    rango = "0",
                    onGrlChange = {},
                    onRangoChange = {},
                    onGrlFocusLost = { sanitizedGrl = it },
                    onRangoFocusLost = {},
                    scrollState = rememberScrollState(),
                )
            }
        }

        composeTestRule.onNodeWithTag("rank_input_1", useUnmergedTree = true).performClick()
        assertEquals("151", sanitizedGrl)
    }
    
    @Test
    fun playerRow_removeButton_isClickableForSubstitutes() {
        var removeClicked = false
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val removeDesc = context.getString(R.string.desc_remove_player, 12)

        composeTestRule.setContent {
            FcmTheme {
                PlayerRow(
                    numero = 12,
                    grl = "100",
                    rango = "0",
                    onGrlChange = {},
                    onRangoChange = {},
                    onGrlFocusLost = {},
                    onRangoFocusLost = {},
                    isSuplente = true,
                    onRemove = { removeClicked = true },
                    scrollState = rememberScrollState(),
                )
            }
        }

        // Buscamos por la descripción exacta obtenida del recurso
        composeTestRule.onNodeWithContentDescription(removeDesc, useUnmergedTree = true).performClick()
        assertTrue(removeClicked)
    }
}
