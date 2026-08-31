package com.kveld9.fcmetrix.domain

import kotlin.math.ceil

/**
 * Lógica pura de cálculo del GRL global de FC Mobile.
 * Centraliza las reglas de negocio y valida la integridad de los datos.
 */
object GrlCalculator {

    const val TITULARES = 11
    const val SUPLENTES_MAX = 7
    
    // Reglas de negocio del dominio
    const val GRL_MIN = 47
    const val GRL_MAX = 150
    const val RANGO_MIN = 0
    const val RANGO_MAX = 5

    /** Un jugador cargado. [grl] es `null` si el campo está incompleto. */
    data class Player(val grl: Int?, val rango: Int)

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
        /** Indica si todos los jugadores usados tienen rango máximo (5). */
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

    /**
     * Realiza el cálculo del GRL global.
     * @throws IllegalArgumentException si algún jugador tiene datos fuera de los límites legales.
     */
    fun calcular(titulares: List<Player>, suplentes: List<Player>): Result {
        val todos = titulares + suplentes
        
        // Validación defensiva de integridad
        todos.forEach { p ->
            if (p.grl != null) {
                require(p.grl in GRL_MIN..GRL_MAX) { "GRL ${p.grl} fuera de rango legal ($GRL_MIN-$GRL_MAX)" }
            }
            require(p.rango in RANGO_MIN..RANGO_MAX) { "Rango ${p.rango} fuera de rango legal ($RANGO_MIN-$RANGO_MAX)" }
        }

        val titCargados = titulares.count { it.grl != null }
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
        
        // Cálculos con tipos enteros
        val sumaBase = usados.sumOf { (it.grl!! - it.rango).toLong() }.toInt()
        val sumaRango = usados.sumOf { it.rango.toLong() }.toInt()

        val avgBase = ceil(sumaBase.toDouble() / n).toInt()
        val avgRango = ceil(sumaRango.toDouble() / n).toInt()

        // Puntos necesarios
        val pBase = puntosParaSubir(sumaBase, n)
        val pRango = if (avgRango >= RANGO_MAX) {
            null
        } else {
            puntosParaSubir(sumaRango, n)
        }

        val esMejoraPorRango = pRango != null && pRango < pBase
        val mejorPuntos = if (esMejoraPorRango) pRango else pBase

        return Result(
            grlGlobal = avgBase + avgRango,
            titularesCargados = titCargados,
            faltantes = 0,
            puntosGrl = pBase,
            puntosRango = pRango,
            rangoMaximo = avgRango >= RANGO_MAX,
            promedioBase = sumaBase.toDouble() / n,
            promedioRango = sumaRango.toDouble() / n,
            puntosSiguienteGrl = mejorPuntos,
            esMejoraPorRango = esMejoraPorRango
        )
    }

    private fun puntosParaSubir(suma: Int, den: Int): Int {
        val promedioActual = ceil(suma.toDouble() / den).toInt()
        return (promedioActual * den) - suma + 1
    }

    /**
     * Ajusta el GRL basándose en el cambio de rango (Base + Rango).
     */
    fun ajustarGrlPorRango(grlActual: Int?, rangoActual: Int, nuevoRango: Int): Int? {
        if (grlActual == null) return null
        val base = grlActual - rangoActual
        return base + nuevoRango
    }
}
