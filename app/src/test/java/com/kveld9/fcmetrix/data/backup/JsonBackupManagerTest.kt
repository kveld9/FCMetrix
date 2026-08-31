package com.kveld9.fcmetrix.data.backup

import com.kveld9.fcmetrix.data.local.entity.TeamEntity
import com.kveld9.fcmetrix.domain.model.PlayerData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class JsonBackupManagerTest {

    private val backupManager = JsonBackupManager()

    @Test
    fun `exportToJson and parseAndValidateJson - roundtrip preserves teams data`() {
        val sampleTeams = listOf(
            TeamEntity(
                id = "team-1",
                name = "Real Madrid",
                titulares = listOf(
                    PlayerData(id = "p1", grl = "110", rango = "5"),
                    PlayerData(id = "p2", grl = "108", rango = "4")
                ),
                suplentes = listOf(
                    PlayerData(id = "s1", grl = "105", rango = "3")
                ),
                lastUpdated = 123456789L
            )
        )

        val json = backupManager.exportToJson(sampleTeams)
        assertTrue(json.contains("Real Madrid"))
        assertTrue(json.contains("FCMetrix"))

        val parsedResult = backupManager.parseAndValidateJson(json)
        assertTrue(parsedResult.isSuccess)

        val parsedTeams = parsedResult.getOrNull()
        assertNotNull(parsedTeams)
        assertEquals(1, parsedTeams!!.size)

        val team = parsedTeams[0]
        assertEquals("team-1", team.id)
        assertEquals("Real Madrid", team.name)
        assertEquals(2, team.titulares.size)
        assertEquals("110", team.titulares[0].grl)
        assertEquals("5", team.titulares[0].rango)
        assertEquals(1, team.suplentes.size)
        assertEquals("105", team.suplentes[0].grl)
    }

    @Test
    fun `readBoundedStream - reads small stream successfully`() {
        val content = "{\"test\": 123}"
        val stream = ByteArrayInputStream(content.toByteArray(Charsets.UTF_8))
        val read = backupManager.readBoundedStream(stream)
        assertEquals(content, read)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `readBoundedStream - throws when maxChars exceeded`() {
        val content = "A".repeat(100)
        val stream = ByteArrayInputStream(content.toByteArray(Charsets.UTF_8))
        backupManager.readBoundedStream(stream, maxChars = 50)
    }

    @Test
    fun `parseAndValidateJson - fails on corrupted JSON`() {
        val result = backupManager.parseAndValidateJson("{ invalid json }")
        assertTrue(result.isFailure)
    }

    @Test
    fun `parseAndValidateJson - fails on future schema version`() {
        val futureJson = """
            {
                "version": 99,
                "exportedAt": "2026-08-27T00:00:00Z",
                "app": "FCMetrix",
                "teams": []
            }
        """.trimIndent()
        val result = backupManager.parseAndValidateJson(futureJson)
        assertTrue(result.isFailure)
    }

    @Test
    fun `parseAndValidateJson - normalizes blank name to Squad and generates id if blank`() {
        val json = """
            {
                "version": 1,
                "exportedAt": "2026-08-27T00:00:00Z",
                "app": "FCMetrix",
                "teams": [
                    {
                        "id": "",
                        "name": "   ",
                        "titulares": [],
                        "suplentes": [],
                        "lastUpdated": 0
                    }
                ]
            }
        """.trimIndent()
        val result = backupManager.parseAndValidateJson(json)
        assertTrue(result.isSuccess)
        val team = result.getOrNull()?.firstOrNull()
        assertNotNull(team)
        assertEquals("Squad", team?.name)
        assertTrue(team?.id?.isNotBlank() == true)
        assertTrue((team?.lastUpdated ?: 0) > 0)
    }
}
