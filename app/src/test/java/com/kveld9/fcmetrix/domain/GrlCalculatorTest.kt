package com.kveld9.fcmetrix.domain

import com.kveld9.fcmetrix.domain.GrlCalculator.GRL_MAX
import com.kveld9.fcmetrix.domain.GrlCalculator.GRL_MIN
import com.kveld9.fcmetrix.domain.GrlCalculator.Player
import com.kveld9.fcmetrix.domain.GrlCalculator.RANGO_MAX
import com.kveld9.fcmetrix.domain.GrlCalculator.RANGO_MIN
import com.kveld9.fcmetrix.domain.GrlCalculator.TITULARES
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GrlCalculatorTest {

    private fun player(grl: Int?, rango: Int = 0) = Player(grl, rango)

    private fun listaCompleta(cantidad: Int, grl: Int, rango: Int = 0) =
        List(cantidad) { player(grl, rango) }

    // region Lineup Combinations
    @Test
    fun `lineup - 11 titulares exactos`() {
        val r = GrlCalculator.calcular(listaCompleta(TITULARES, 100, 0), emptyList())
        assertEquals(100, r.grlGlobal)
        assertEquals(TITULARES, r.titularesCargados)
        assertEquals(0, r.faltantes)
    }

    @Test
    fun `lineup - 11 mas 1 suplente`() {
        val t = listaCompleta(TITULARES, 100, 0)
        val s = listOf(player(112, 0)) // Suma = 1100 + 112 = 1212. n = 12. avg = 101.
        val r = GrlCalculator.calcular(t, s)
        assertEquals(101, r.grlGlobal)
    }

    @Test
    fun `lineup - 11 mas 7 suplentes`() {
        val t = listaCompleta(TITULARES, 100, 0)
        val s = listaCompleta(7, 100, 0) // n = 18. avg = 100.
        val r = GrlCalculator.calcular(t, s)
        assertEquals(100, r.grlGlobal)
    }

    @Test
    fun `lineup - heterogeneous starters and subs`() {
        val t = List(11) { i -> player(90 + i, 0) } // sum = 990 + 55 = 1045
        val s = listOf(player(110, 5), player(105, 2)) // n = 13. sumBase = 1045 + 105 + 103 = 1253. avg = 97. sumRango = 7. avg = 1.
        val r = GrlCalculator.calcular(t, s)
        assertEquals(97 + 1, r.grlGlobal)
    }

    @Test
    fun `lineup - order independence`() {
        val p1 = player(100, 5)
        val p2 = player(90, 0)
        val p3 = player(110, 2)
        val rest = listaCompleta(8, 100, 0)

        val r1 = GrlCalculator.calcular(listOf(p1, p2, p3) + rest, emptyList())
        val r2 = GrlCalculator.calcular(listOf(p3, p1, p2) + rest, emptyList())
        val r3 = GrlCalculator.calcular(rest + listOf(p2, p3, p1), emptyList())

        assertEquals(r1.grlGlobal, r2.grlGlobal)
        assertEquals(r2.grlGlobal, r3.grlGlobal)
    }

    @Test
    fun `lineup - incomplete lineup returns null results`() {
        val t = listaCompleta(5, 100)
        val r = GrlCalculator.calcular(t, emptyList())
        assertNull(r.grlGlobal)
        assertEquals(5, r.titularesCargados)
        assertEquals(6, r.faltantes)
    }
    // endregion

    // region Rounding Logic
    @Test
    fun `rounding - independent ceil for base and range`() {
        val t1 = List(10) { player(100, 0) } + player(101, 1)
        val r1 = GrlCalculator.calcular(t1, emptyList())
        assertEquals(101, r1.grlGlobal) // avgBase 100, avgRango 0.09 -> 101
        
        val t2 = List(10) { player(100, 0) } + player(102, 1)
        val r2 = GrlCalculator.calcular(t2, emptyList())
        assertEquals(102, r2.grlGlobal) // avgBase 100.09 -> 101, avgRango 0.09 -> 1
    }

    @Test
    fun `rounding - base boundaries`() {
        val r100Exact = GrlCalculator.calcular(listaCompleta(TITULARES, 100), emptyList())
        assertEquals(100, r100Exact.grlGlobal)
        assertEquals(1, r100Exact.puntosGrl)

        val r100LowDecimal = GrlCalculator.calcular(listaCompleta(10, 100) + player(101), emptyList())
        assertEquals(101, r100LowDecimal.grlGlobal)
        assertEquals(11, r100LowDecimal.puntosGrl)

        val r100MidDecimal = GrlCalculator.calcular(listaCompleta(6, 100) + listaCompleta(5, 101), emptyList())
        assertEquals(101, r100MidDecimal.grlGlobal)
        assertEquals(7, r100MidDecimal.puntosGrl)

        val r100HighDecimal = GrlCalculator.calcular(listaCompleta(1, 100) + listaCompleta(10, 101), emptyList())
        assertEquals(101, r100HighDecimal.grlGlobal)
        assertEquals(2, r100HighDecimal.puntosGrl)
    }

    @Test
    fun `rounding - range boundaries`() {
        val rMax = GrlCalculator.calcular(listaCompleta(TITULARES, 105, 5), emptyList())
        assertEquals(105, rMax.grlGlobal)
        assertTrue(rMax.rangoMaximo)
        assertNull(rMax.puntosRango)

        val r4Exact = GrlCalculator.calcular(listaCompleta(TITULARES, 104, 4), emptyList())
        assertEquals(104, r4Exact.grlGlobal)
        assertEquals(1, r4Exact.puntosRango)
    }
    // endregion

    // region Recommendations
    @Test
    fun `recommendations - tie break prefers base`() {
        val r = GrlCalculator.calcular(listaCompleta(TITULARES, 100, 0), emptyList())
        assertFalse("Should prefer base on tie", r.esMejoraPorRango)
    }

    @Test
    fun `recommendations - range is better`() {
        val t = listaCompleta(10, 100) + player(90, 0)
        val r = GrlCalculator.calcular(t, emptyList())
        assertTrue(r.esMejoraPorRango)
    }
    // endregion

    // region Properties
    @Test
    fun `properties - monotonicity`() {
        val t1 = listaCompleta(11, 100)
        val r1 = GrlCalculator.calcular(t1, emptyList())
        
        val tPlus = listaCompleta(10, 100) + player(101)
        val r2 = GrlCalculator.calcular(tPlus, emptyList())
        
        assertTrue(r2.grlGlobal!! >= r1.grlGlobal!!)
    }

    @Test
    fun `properties - uniformity`() {
        for (n in 11..18) {
            val players = listaCompleta(n, 105, 3)
            val r = GrlCalculator.calcular(players.take(11), players.drop(11))
            assertEquals(105, r.grlGlobal)
        }
    }

    @Test
    fun `properties - neutrality`() {
        val t = listaCompleta(11, 100, 0)
        val r1 = GrlCalculator.calcular(t, emptyList())
        val r2 = GrlCalculator.calcular(t, listOf(player(100, 0)))
        assertEquals(r1.grlGlobal, r2.grlGlobal)
    }
    // endregion

    // region Invariants
    @Test(expected = IllegalArgumentException::class)
    fun `invariants - grl above max throws`() {
        GrlCalculator.calcular(listOf(player(GRL_MAX + 1)), emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invariants - grl below min throws`() {
        GrlCalculator.calcular(listOf(player(GRL_MIN - 1)), emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invariants - range above max throws`() {
        GrlCalculator.calcular(listOf(player(100, RANGO_MAX + 1)), emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invariants - range below min throws`() {
        GrlCalculator.calcular(listOf(player(100, RANGO_MIN - 1)), emptyList())
    }
    // endregion

    // region Utilities
    @Test
    fun `utilities - ajustarGrlPorRango`() {
        assertEquals(105, GrlCalculator.ajustarGrlPorRango(100, 0, 5))
        assertEquals(95, GrlCalculator.ajustarGrlPorRango(98, 3, 0))
        assertNull(GrlCalculator.ajustarGrlPorRango(null, 0, 5))
    }
    // endregion
}
