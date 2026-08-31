package com.kveld9.fcmetrix.ui.model

import com.kveld9.fcmetrix.domain.GrlCalculator
import com.kveld9.fcmetrix.domain.model.PlayerData

/**
 * Estado completo de la pantalla de cálculo de GRL.
 */

data class GrlUiState(
    val teamName: String? = null,
    val titulares: List<PlayerData> = List(GrlCalculator.TITULARES) { PlayerData() },
    val suplentes: List<PlayerData> = emptyList(),
    val result: GrlCalculator.Result = GrlCalculator.Result(
        grlGlobal = null,
        titularesCargados = 0,
        faltantes = GrlCalculator.TITULARES,
        puntosGrl = null,
        puntosRango = null
    )
)
