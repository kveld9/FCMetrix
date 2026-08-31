package com.kveld9.fcmetrix.data

import com.kveld9.fcmetrix.data.backup.DuplicatePolicy
import com.kveld9.fcmetrix.data.local.dao.LineupDao
import com.kveld9.fcmetrix.data.local.entity.TeamEntity
import com.kveld9.fcmetrix.domain.model.PlayerData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LineupRepositoryTest {

    private lateinit var repository: LineupRepository
    private lateinit var fakeDao: FakeLineupDao

    class FakeLineupDao : LineupDao {
        val teams = mutableMapOf<String, TeamEntity>()

        override fun getAllTeams(): Flow<List<TeamEntity>> = MutableStateFlow(teams.values.toList())
        override suspend fun getAllTeamsSync(): List<TeamEntity> = teams.values.toList()
        override suspend fun getTeamById(id: String): TeamEntity? = teams[id]
        override suspend fun insertTeam(team: TeamEntity) {
            teams[team.id] = team
        }
        override suspend fun insertTeams(teams: List<TeamEntity>) {
            teams.forEach { this.teams[it.id] = it }
        }
        override suspend fun deleteTeam(id: String) {
            teams.remove(id)
        }
        override suspend fun deleteAllTeams() {
            teams.clear()
        }
    }

    @Before
    fun setup() {
        fakeDao = FakeLineupDao()
        repository = LineupRepository(fakeDao)
    }

    @Test
    fun `saveTeam and getTeam - stores and retrieves team correctly`() = runTest {
        repository.saveTeam(
            id = "t1",
            name = "Dream Team",
            titulares = listOf(PlayerData(grl = "110", rango = "5")),
            suplentes = emptyList()
        )

        val retrieved = repository.getTeam("t1")
        assertEquals("Dream Team", retrieved?.name)
        assertEquals(1, retrieved?.titulares?.size)
    }

    @Test
    fun `importTeams - OVERWRITE_ALL replaces existing team with same id and inserts new ones`() = runTest {
        repository.saveTeam("t1", "Old Name", listOf(PlayerData(grl = "90")), emptyList())

        val importList = listOf(
            TeamEntity(id = "t1", name = "New Name", titulares = listOf(PlayerData(grl = "110")), suplentes = emptyList(), lastUpdated = 200L),
            TeamEntity(id = "t2", name = "Second Team", titulares = emptyList(), suplentes = emptyList(), lastUpdated = 300L)
        )

        val (imported, skipped) = repository.importTeams(importList, DuplicatePolicy.OVERWRITE_ALL)

        assertEquals(2, imported)
        assertEquals(0, skipped)
        assertEquals("New Name", repository.getTeam("t1")?.name)
        assertEquals("Second Team", repository.getTeam("t2")?.name)
    }

    @Test
    fun `importTeams - SKIP_EXISTING ignores duplicates by ID or case-insensitive Name`() = runTest {
        repository.saveTeam("t1", "Real Madrid", listOf(PlayerData(grl = "90")), emptyList())

        val importList = listOf(
            TeamEntity(id = "t1", name = "Different Name", titulares = emptyList(), suplentes = emptyList(), lastUpdated = 100L),
            TeamEntity(id = "t2", name = "  real madrid  ", titulares = emptyList(), suplentes = emptyList(), lastUpdated = 100L),
            TeamEntity(id = "t3", name = "FC Barcelona", titulares = emptyList(), suplentes = emptyList(), lastUpdated = 100L)
        )

        val (imported, skipped) = repository.importTeams(importList, DuplicatePolicy.SKIP_EXISTING)

        assertEquals(1, imported)
        assertEquals(2, skipped)
        assertEquals("Real Madrid", repository.getTeam("t1")?.name)
        assertEquals(null, repository.getTeam("t2"))
        assertEquals("FC Barcelona", repository.getTeam("t3")?.name)
    }

    @Test
    fun `importTeams - DUPLICATE_ALL generates new IDs for collisions and keeps all items`() = runTest {
        repository.saveTeam("t1", "Original Team", listOf(PlayerData(grl = "90")), emptyList())

        val importList = listOf(
            TeamEntity(id = "t1", name = "Imported Clone", titulares = listOf(PlayerData(grl = "105")), suplentes = emptyList(), lastUpdated = 100L),
            TeamEntity(id = "t2", name = "Brand New Team", titulares = emptyList(), suplentes = emptyList(), lastUpdated = 100L)
        )

        val (imported, skipped) = repository.importTeams(importList, DuplicatePolicy.DUPLICATE_ALL)

        assertEquals(2, imported)
        assertEquals(0, skipped)

        val all = repository.getAllTeamsSync()
        assertEquals(3, all.size)
        // Original t1 is still there
        assertEquals("Original Team", repository.getTeam("t1")?.name)
        // Clone is present under a new UUID
        val clone = all.first { it.name == "Imported Clone" }
        assertNotEquals("t1", clone.id)
        assertTrue(clone.id.isNotBlank())
    }
}
