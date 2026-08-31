package com.kveld9.fcmetrix.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Immutable player representation for domain, persistence, and state.
 */
@Serializable
data class PlayerData(
    val id: String = UUID.randomUUID().toString(),
    val grl: String = "",
    val rango: String = "0"
)
