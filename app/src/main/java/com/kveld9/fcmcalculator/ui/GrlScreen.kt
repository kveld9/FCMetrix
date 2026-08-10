package com.kveld9.fcmcalculator.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kveld9.fcmcalculator.R
import com.kveld9.fcmcalculator.domain.GrlCalculator
import com.kveld9.fcmcalculator.ui.components.Actions
import com.kveld9.fcmcalculator.ui.components.Footer
import com.kveld9.fcmcalculator.ui.components.GrlCard
import com.kveld9.fcmcalculator.ui.components.Header
import com.kveld9.fcmcalculator.ui.components.ProgressSection
import com.kveld9.fcmcalculator.ui.components.RangoQuickChips
import com.kveld9.fcmcalculator.ui.components.SectionHeader
import com.kveld9.fcmcalculator.ui.components.SuplentesList
import com.kveld9.fcmcalculator.ui.components.TitularesList
import com.kveld9.fcmcalculator.ui.viewmodel.GrlViewModel

@Composable
fun GrlScreen(
    viewModel: GrlViewModel,
    modifier: Modifier = Modifier,
    dynamicColor: Boolean = false,
    onDynamicColorChange: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .statusBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Header()

            GrlCard(result = uiState.result)

            ProgressSection(titularesCargados = uiState.result.titularesCargados)

            RangoQuickChips(
                onChange = { value -> viewModel.updateAllRanks(value) },
            )

            SectionHeader(
                title = stringResource(R.string.titulares),
                badge = "${uiState.result.titularesCargados}/11"
            )
            TitularesList(
                titulares = uiState.titulares,
                onGrlChange = viewModel::onGrlChanged,
                onRangoChange = viewModel::onRangoChanged,
                onGrlFocusLost = viewModel::onGrlFocusLost,
                scrollState = scrollState
            )

            SectionHeader(
                title = stringResource(R.string.suplentes),
                badge = "${uiState.suplentes.size}/${GrlCalculator.SUPLENTES_MAX}",
            )
            SuplentesList(
                suplentes = uiState.suplentes,
                onGrlChange = viewModel::onGrlChanged,
                onRangoChange = viewModel::onRangoChanged,
                onGrlFocusLost = viewModel::onGrlFocusLost,
                onRemove = viewModel::removeSubstitute,
                scrollState = scrollState,
            )

            Actions(
                onAddSuplente = { viewModel.addSubstitute() },
                onClearAll = { viewModel.clearAll() },
            )

            Footer()
        }

        // Toggle Monet
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                IconButton(
                    onClick = { onDynamicColorChange(!dynamicColor) },
                ) {
                    Icon(
                        imageVector = if (dynamicColor) Icons.Filled.Palette else Icons.Outlined.Palette,
                        contentDescription = stringResource(R.string.dynamic_color),
                        tint = if (dynamicColor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
