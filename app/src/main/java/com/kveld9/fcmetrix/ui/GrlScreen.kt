package com.kveld9.fcmetrix.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kveld9.fcmetrix.R
import com.kveld9.fcmetrix.domain.GrlCalculator
import com.kveld9.fcmetrix.ui.components.Actions
import com.kveld9.fcmetrix.ui.components.GrlCard
import com.kveld9.fcmetrix.ui.components.GrlPreviewDialog
import com.kveld9.fcmetrix.ui.components.Header
import com.kveld9.fcmetrix.ui.components.RangoQuickChips
import com.kveld9.fcmetrix.ui.components.SectionHeader
import com.kveld9.fcmetrix.ui.components.SuplentesList
import com.kveld9.fcmetrix.ui.components.TeamListSheet
import com.kveld9.fcmetrix.ui.components.TitularesList
import com.kveld9.fcmetrix.ui.util.ShareProvider
import com.kveld9.fcmetrix.ui.viewmodel.GrlViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun GrlScreen(
    viewModel: GrlViewModel,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allTeams by viewModel.allTeams.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showTeamManager by remember { mutableStateOf(false) }
    var capturedBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    val scope = rememberCoroutineScope()

    val saveSuccessMsg = stringResource(R.string.save_success)
    val saveErrorMsg = stringResource(R.string.save_error)
    val clampingGrlFormat = stringResource(R.string.clamping_grl)
    val clampingRankFormat = stringResource(R.string.clamping_rank)

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            val message = when (event) {
                is GrlViewModel.UiEvent.ShowGrlClamping -> 
                    String.format(clampingGrlFormat, event.min, event.max)
                is GrlViewModel.UiEvent.ShowRankClamping -> 
                    String.format(clampingRankFormat, event.min, event.max)
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            Header(
                onOpenSettings = onOpenSettings,
                onShowTeamManager = { showTeamManager = true }
            )

            GrlCard(
                result = uiState.result,
                teamName = uiState.teamName,
                onCapture = { capturedBitmap = it }
            )

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
                onRangoFocusLost = viewModel::onRangoFocusLost,
            )

            SectionHeader(
                title = stringResource(R.string.suplentes),
                badge = "${uiState.suplentes.size}/${GrlCalculator.SUPLENTES_MAX}",
                modifier = Modifier.padding(top = 16.dp)
            )
            SuplentesList(
                suplentes = uiState.suplentes,
                onGrlChange = viewModel::onGrlChanged,
                onRangoChange = viewModel::onRangoChanged,
                onGrlFocusLost = viewModel::onGrlFocusLost,
                onRangoFocusLost = viewModel::onRangoFocusLost,
                onRemove = viewModel::removeSubstitute,
            )

            Actions(
                onAddSuplente = { viewModel.addSubstitute() },
                onClearAll = { viewModel.clearAll() },
            )
        }

        if (showTeamManager) {
            TeamListSheet(
                teams = allTeams,
                onLoad = viewModel::loadTeam,
                onDelete = viewModel::deleteTeam,
                onSaveCurrent = viewModel::saveCurrentTeam,
                onDismiss = { showTeamManager = false }
            )
        }

        capturedBitmap?.let { bitmap ->
            GrlPreviewDialog(
                bitmap = bitmap,
                onDownload = {
                    val success = ShareProvider.saveBitmapToGallery(context, bitmap)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (success) saveSuccessMsg else saveErrorMsg
                        )
                    }
                    capturedBitmap = null
                },
                onShare = {
                    ShareProvider.shareBitmap(context, bitmap)
                    capturedBitmap = null
                },
                onDismiss = { capturedBitmap = null }
            )
        }
    }
}
}
