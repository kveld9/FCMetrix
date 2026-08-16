package com.kveld9.fcmetrix.data

import com.kveld9.fcmetrix.data.local.dao.LineupDao
import com.kveld9.fcmetrix.data.local.entity.TeamEntity
import com.kveld9.fcmetrix.ui.model.PlayerData
import kotlinx.coroutines.flow.Flow

class LineupRepository(private val lineupDao: LineupDao) {

    val allTeams: Flow<List<TeamEntity>> = lineupDao.getAllTeams()

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

    suspend fun deleteTeam(id: String) = lineupDao.deleteTeam(id)
}
