package com.kveld9.fcmcalculator.domain

import kotlin.math.ceil

/**
 * Lógica pura de cálculo del GRL global de FC Mobile.
 *
 * Ported 1:1 from the previous JavaScript implementation in `assets/index.html`
 * so behavior stays identical. No Android dependencies: unit-testable.
 */
object GrlCalculator {

    const val TITULARES = 11
    const val SUPLENTES_MAX = 7

    /** Un jugador cargado. [grl] es `null` si el campo quedó vacío. */
    data class Player(val grl: Double?, val rango: Double)

    /** Resultado del cálculo en un momento dado. */
    data class Result(
        /** GRL global redondeado hacia arriba, o `null` si faltan titulares. */
        val grlGlobal: Int?,
        /** Cuántos titulares tienen GRL cargado. */
        val titularesCargados: Int,
        /** Cuántos jugadores más se necesitan para completar los 11. */
        val faltantes: Int,
        /** Puntos de GRL necesarios para subir, o `null` si incompleto. */
        val puntosGrl: Int?,
        /** Puntos de rango necesarios para subir, o `null` si incompleto. */
        val puntosRango: Int?,
        /** Indica si todos los jugadores usados tienen rango 5. */
        val rangoMaximo: Boolean = false,
    )

    fun calcular(titulares: List<Player>, suplentes: List<Player>): Result {
        val titCargados = titulares.count { it.grl != null }
        val todos = titulares + suplentes
        val totalCargados = todos.count { it.grl != null }

        if (totalCargados < TITULARES) {
            return Result(
                grlGlobal = null,
                titularesCargados = titCargados,
                faltantes = TITULARES - titCargados,
                puntosGrl = null,
                puntosRango = null,
            )
        }

        val n = TITULARES + suplentes.count { it.grl != null }
        val usados = todos.filter { it.grl != null }
        val sumaBase = usados.sumOf { it.grl!! - it.rango }
        val sumaRango = usados.sumOf { it.rango }

        val avgBase = ceil(sumaBase / n).toInt()
        val avgRango = ceil(sumaRango / n).toInt()

        val pRango = if (avgRango >= 5) {
            val totalMaximo = n * 5
            val faltanParaMax = (totalMaximo - sumaRango).toInt()
            if (faltanParaMax > 0) faltanParaMax else null
        } else {
            puntosParaSubir(sumaRango, n)
        }

        return Result(
            grlGlobal = avgBase + avgRango,
            titularesCargados = titCargados,
            faltantes = 0,
            puntosGrl = puntosParaSubir(sumaBase, n),
            puntosRango = pRango,
            rangoMaximo = usados.all { it.rango == 5.0 }
        )
    }

    private fun puntosParaSubir(suma: Double, den: Int): Int {
        val promedioActual = ceil(suma / den).toInt()
        return (promedioActual * den) - suma.toInt() + 1
    }
}