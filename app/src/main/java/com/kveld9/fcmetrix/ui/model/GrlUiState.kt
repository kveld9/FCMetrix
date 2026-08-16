package com.kveld9.fcmetrix.ui.model

import com.kveld9.fcmetrix.domain.GrlCalculator
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Datos inmutables de un jugador para la UI.
 */
@Serializable
data class PlayerData(
    val id: String = UUID.randomUUID().toString(),
    val grl: String = "",
    val rango: String = "0"
)

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
