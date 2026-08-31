package com.kveld9.fcmetrix.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kveld9.fcmetrix.data.local.entity.TeamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LineupDao {
    @Query("SELECT * FROM teams ORDER BY lastUpdated DESC")
    fun getAllTeams(): Flow<List<TeamEntity>>

    @Query("SELECT * FROM teams ORDER BY lastUpdated DESC")
    suspend fun getAllTeamsSync(): List<TeamEntity>

    @Query("SELECT * FROM teams WHERE id = :id LIMIT 1")
    suspend fun getTeamById(id: String): TeamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: TeamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<TeamEntity>)

    @Query("DELETE FROM teams WHERE id = :id")
    suspend fun deleteTeam(id: String)

    @Query("DELETE FROM teams")
    suspend fun deleteAllTeams()
}
