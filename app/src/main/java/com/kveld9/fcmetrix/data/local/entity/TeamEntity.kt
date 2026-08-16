package com.kveld9.fcmetrix.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kveld9.fcmetrix.ui.model.PlayerData

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val titulares: List<PlayerData>,
    val suplentes: List<PlayerData>,
    val lastUpdated: Long = System.currentTimeMillis()
)
