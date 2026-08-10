package com.android.fcmcalculator.domain

import com.android.fcmcalculator.domain.GrlCalculator.Player
import com.android.fcmcalculator.domain.GrlCalculator.SUPLENTES_MAX
import com.android.fcmcalculator.domain.GrlCalculator.TITULARES
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GrlCalculatorTest {

    private fun titulares(vararg players: Pair<Double?, Double>): List<Player> =
        players.map { Player(it.first, it.second) }

    private fun listaCompleta(cantidad: Int, grl: Double, rango: Double = 0.0) =
        List(cantidad) { Player(grl, rango) }

    @Test
    fun `todos en 100, rango 0 - global 100`() {
        val r = GrlCalculator.calcular(listaCompleta(TITULARES, 100.0), emptyList())
        assertEquals(100, r.grlGlobal)
        assertEquals(TITULARES, r.titularesCargados)
        assertEquals(0, r.faltantes)
        assertEquals(1, r.puntosGrl)
        assertEquals(1, r.puntosRango)
    }

    @Test
    fun `un titular en 99 baja a la decima y pide 2 puntos`() {
        val t = listaCompleta(10, 100.0) + Player(99.0, 0.0)
        val r = GrlCalculator.calcular(t, emptyList())
        assertEquals(100, r.grlGlobal)
        assertEquals(2, r.puntosGrl)
        assertEquals(1, r.puntosRango)
    }

    @Test
    fun `incompleto - global null y faltantes correctos`() {
        val t = listaCompleta(5, 100.0)
        val r = GrlCalculator.calcular(t, emptyList())
        assertNull(r.grlGlobal)
        assertNull(r.puntosGrl)
        assertNull(r.puntosRango)
        assertEquals(6, r.faltantes)
        assertEquals(5, r.titularesCargados)
    }

    @Test
    fun `suplente sube el denominador`() {
        val t = listaCompleta(TITULARES, 100.0)
        val s = listOf(Player(110.0, 0.0))
        val r = GrlCalculator.calcular(t, s)
        assertEquals(101, r.grlGlobal)
        assertEquals(3, r.puntosGrl)
        assertEquals(1, r.puntosRango)
    }

    @Test
    fun `suplente con grl vacio no cuenta en el calculo`() {
        val t = listaCompleta(TITULARES, 100.0)
        val s = listOf(Player(null, 2.0))
        val r = GrlCalculator.calcular(t, s)
        assertEquals(100, r.grlGlobal)
        assertEquals(TITULARES, r.titularesCargados)
        assertEquals(0, r.faltantes)
    }

    @Test
    fun `rangos uniformes se cancelan en el global`() {
        val t = listaCompleta(TITULARES, 100.0, rango = 3.0)
        val r = GrlCalculator.calcular(t, emptyList())
        // sumaBase = 11*97 = 1067 -> ceil/11 = 97; sumaRango = 33 -> ceil/11 = 3
        assertEquals(100, r.grlGlobal)
        assertEquals(1, r.puntosGrl)
    }

    @Test
    fun `grupos piden un punto cuando el promedio es exacto`() {
        val t = listaCompleta(TITULARES, 100.0)
        val s = listOf(Player(100.0, 0.0))
        // n = 12, rango 0 -> solo importan los grupos base? No: sumaBase y sumaRango.
        val r = GrlCalculator.calcular(t, s)
        // sumaBase = 11*100 + 100 = 1200, n = 12 -> 100 exacto -> pide 1
        assertEquals(100, r.grlGlobal)
        assertEquals(1, r.puntosGrl)
    }

    @Test
    fun `titulares via suplentes completan el minimo`() {
        // 11 en total pero con solo 10 titulares cargados + 1 suplente cargado
        val t = listaCompleta(10, 100.0)
        val s = listOf(Player(100.0, 0.0))
        val r = GrlCalculator.calcular(t, s)
        // n = 12, sumaBase = 1100 -> ceil(91.66) = 92
        assertEquals(92, r.grlGlobal)
        assertEquals(0, r.faltantes)
        assertEquals(10, r.titularesCargados)
    }

    @Test
    fun `maximo de suplentes es 7 y titulares 11`() {
        assertEquals(11, TITULARES)
        assertEquals(7, SUPLENTES_MAX)
    }
}