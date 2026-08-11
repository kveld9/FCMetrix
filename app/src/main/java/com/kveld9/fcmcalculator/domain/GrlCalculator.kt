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
        /** Promedio decimal del GRL Base (sin redondear). */
        val promedioBase: Double? = null,
        /** Promedio decimal del Rango (sin redondear). */
        val promedioRango: Double? = null,
        /** Cantidad mínima de puntos (de cualquier tipo) para subir el GRL global. */
        val puntosSiguienteGrl: Int? = null,
        /** Si la mejor mejora es por rango. */
        val esMejoraPorRango: Boolean = false,
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

        // Si el promedio de rango ya es 5, ya no se puede subir más el GRL global por esta vía.
        val pRango = if (avgRango >= 5) {
            null
        } else {
            puntosParaSubir(sumaRango, n)
        }

        val pBase = puntosParaSubir(sumaBase, n)
        val mejorPuntos = if (pRango != null && pRango < pBase) pRango else pBase

        return Result(
            grlGlobal = avgBase + avgRango,
            titularesCargados = titCargados,
            faltantes = 0,
            puntosGrl = pBase,
            puntosRango = pRango,
            rangoMaximo = avgRango >= 5,
            promedioBase = sumaBase / n,
            promedioRango = sumaRango / n,
            puntosSiguienteGrl = mejorPuntos,
            esMejoraPorRango = pRango != null && pRango < pBase
        )
    }

    private fun puntosParaSubir(suma: Double, den: Int): Int {
        val promedioActual = ceil(suma / den).toInt()
        return (promedioActual * den) - suma.toInt() + 1
    }

    /**
     * Ajusta el GRL de un jugador basándose en el cambio de rango.
     * En FC Mobile, el GRL visible del jugador es GRL_BASE + RANGO.
     */
    fun ajustarGrlPorRango(grlActual: String, rangoActual: String, nuevoRango: String): String {
        val g = grlActual.toDoubleOrNull() ?: return ""
        val r = rangoActual.toDoubleOrNull() ?: 0.0
        val nextR = nuevoRango.toDoubleOrNull() ?: 0.0

        val base = g - r
        return (base + nextR).toInt().toString()
    }

    /**
     * Valida y sanea la entrada de GRL.
     */
    fun sanearGrl(input: String): String {
        if (input.isBlank()) return ""
        val numeric = input.filter { it.isDigit() }.toIntOrNull() ?: return ""
        return when {
            numeric < 47 -> "47"
            numeric > 150 -> "150"
            else -> numeric.toString()
        }
    }

    /**
     * Valida y sanea la entrada de Rango.
     */
    fun sanearRango(input: String): String {
        if (input.isBlank()) return "0"
        val numeric = input.filter { it.isDigit() }.toIntOrNull() ?: return "0"
        return when {
            numeric < 0 -> "0"
            numeric > 5 -> "5"
            else -> numeric.toString()
        }
    }
}
