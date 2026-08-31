package com.kveld9.fcmetrix.data.local.converter

import com.kveld9.fcmetrix.domain.model.PlayerData
import org.junit.Assert.assertEquals
import org.junit.Test

class LineupConvertersTest {

    private val converters = LineupConverters()

    @Test
    fun `serialization - converts list to json string and back`() {
        val players = listOf(
            PlayerData(id = "1", grl = "100", rango = "5"),
            PlayerData(id = "2", grl = "90", rango = "0")
        )

        val json = converters.fromPlayerDataList(players)
        val result = converters.toPlayerDataList(json)

        assertEquals(players, result)
        assertEquals(2, result.size)
        assertEquals("100", result[0].grl)
    }

    @Test
    fun `serialization - handles empty list`() {
        val players = emptyList<PlayerData>()
        val json = converters.fromPlayerDataList(players)
        val result = converters.toPlayerDataList(json)
        assertEquals(players, result)
    }
}
