package com.kveld9.fcmetrix.data.backup

import com.kveld9.fcmetrix.data.local.entity.TeamEntity
import com.kveld9.fcmetrix.domain.model.PlayerData
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

enum class DuplicatePolicy {
    OVERWRITE_ALL,
    SKIP_EXISTING,
    DUPLICATE_ALL
}

@Serializable
data class BackupDto(
    val version: Int = 1,
    val exportedAt: String,
    val app: String = "FCMetrix",
    val teams: List<TeamBackupDto>
)

@Serializable
data class TeamBackupDto(
    val id: String,
    val name: String,
    val titulares: List<PlayerData>,
    val suplentes: List<PlayerData>,
    val lastUpdated: Long
)

class JsonBackupManager {

    companion object {
        const val MAX_JSON_SIZE_CHARS = 5 * 1024 * 1024 // 5 MB
        const val MAX_RECORDS = 500
    }

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun readBoundedStream(inputStream: InputStream, maxChars: Int = MAX_JSON_SIZE_CHARS): String {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val buffer = CharArray(8192)
        val builder = StringBuilder()
        var totalChars = 0
        var charsRead: Int
        while (reader.read(buffer).also { charsRead = it } != -1) {
            totalChars += charsRead
            if (totalChars > maxChars) {
                throw IllegalArgumentException("El archivo de respaldo excede el tamaño máximo permitido (5 MB).")
            }
            builder.append(buffer, 0, charsRead)
        }
        return builder.toString()
    }

    fun exportToJson(teams: List<TeamEntity>): String {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val backupDto = BackupDto(
            version = 1,
            exportedAt = isoFormat.format(Date()),
            app = "FCMetrix",
            teams = teams.map {
                TeamBackupDto(
                    id = it.id,
                    name = it.name,
                    titulares = it.titulares,
                    suplentes = it.suplentes,
                    lastUpdated = it.lastUpdated
                )
            }
        )
        return json.encodeToString(BackupDto.serializer(), backupDto)
    }

    fun parseAndValidateJson(jsonString: String): Result<List<TeamEntity>> {
        return runCatching {
            if (jsonString.length > MAX_JSON_SIZE_CHARS) {
                throw IllegalArgumentException("El archivo de respaldo excede el tamaño máximo permitido (5 MB).")
            }

            val dto = json.decodeFromString(BackupDto.serializer(), jsonString)
            if (dto.version > 1) {
                throw IllegalArgumentException("Versión de esquema no soportada: ${dto.version}")
            }
            if (dto.teams.size > MAX_RECORDS) {
                throw IllegalArgumentException("El archivo contiene demasiados equipos (${dto.teams.size} > $MAX_RECORDS).")
            }

            dto.teams.map { item ->
                TeamEntity(
                    id = if (item.id.isNotBlank()) item.id else UUID.randomUUID().toString(),
                    name = item.name.trim().ifBlank { "Squad" },
                    titulares = item.titulares,
                    suplentes = item.suplentes,
                    lastUpdated = if (item.lastUpdated > 0) item.lastUpdated else System.currentTimeMillis()
                )
            }
        }
    }
}
