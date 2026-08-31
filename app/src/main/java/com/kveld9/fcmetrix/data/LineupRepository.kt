package com.kveld9.fcmetrix.data

import com.kveld9.fcmetrix.data.local.dao.LineupDao
import com.kveld9.fcmetrix.data.local.entity.TeamEntity
import com.kveld9.fcmetrix.domain.model.PlayerData
import kotlinx.coroutines.flow.Flow

import com.kveld9.fcmetrix.data.backup.DuplicatePolicy
import java.util.UUID

class LineupRepository(private val lineupDao: LineupDao) {

    val allTeams: Flow<List<TeamEntity>> = lineupDao.getAllTeams()

    suspend fun getAllTeamsSync(): List<TeamEntity> = lineupDao.getAllTeamsSync()

    suspend fun getTeam(id: String) = lineupDao.getTeamById(id)

    suspend fun saveTeam(id: String, name: String, titulares: List<PlayerData>, suplentes: List<PlayerData>) {
        val team = TeamEntity(
            id = id,
            name = name,
            titulares = titulares,
            suplentes = suplentes,
            lastUpdated = System.currentTimeMillis()
        )
        lineupDao.insertTeam(team)
    }

    suspend fun insertTeams(teams: List<TeamEntity>) = lineupDao.insertTeams(teams)

    suspend fun importTeams(newTeams: List<TeamEntity>, policy: DuplicatePolicy): Pair<Int, Int> {
        val existing = getAllTeamsSync()
        val existingIds = existing.map { it.id }.toSet()
        val existingNames = existing.map { it.name.lowercase().trim() }.toSet()

        return when (policy) {
            DuplicatePolicy.OVERWRITE_ALL -> {
                insertTeams(newTeams)
                Pair(newTeams.size, 0)
            }
            DuplicatePolicy.SKIP_EXISTING -> {
                val (toSkip, toInsert) = newTeams.partition {
                    existingIds.contains(it.id) || existingNames.contains(it.name.lowercase().trim())
                }
                if (toInsert.isNotEmpty()) {
                    insertTeams(toInsert)
                }
                Pair(toInsert.size, toSkip.size)
            }
            DuplicatePolicy.DUPLICATE_ALL -> {
                val toInsert = newTeams.map { team ->
                    if (existingIds.contains(team.id)) {
                        team.copy(id = UUID.randomUUID().toString())
                    } else {
                        team
                    }
                }
                if (toInsert.isNotEmpty()) {
                    insertTeams(toInsert)
                }
                Pair(toInsert.size, 0)
            }
        }
    }

    suspend fun deleteTeam(id: String) = lineupDao.deleteTeam(id)

    suspend fun deleteAllTeams() = lineupDao.deleteAllTeams()
}
