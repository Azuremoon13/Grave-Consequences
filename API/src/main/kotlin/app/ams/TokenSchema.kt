package app.ams

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.*
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.SecureRandom
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class TokenService {
    private val config = ConfigManager.config
    object Tokens : IntIdTable() {
        val token = varchar("token", 255)
        val generatedAt = varchar("generatedAt",255)
        val createdBy = varchar("createdBy",255)
    }

    init {
        transaction {
            SchemaUtils.create(Tokens)
        }
        CoroutineScope(Dispatchers.IO).launch { initTokens() }
    }

    private suspend fun initTokens(){
        var tokensGenerated = false
        val tokens = transaction {
            Tokens
                .select(Tokens.token, Tokens.createdBy)
                .filter { it[Tokens.createdBy] == "API" }
                .map { it[Tokens.token] }.toMutableList()
        }
        while (tokens.count() < ConfigManager.config.initialTokenCount){
            val token = create()
            GCLogger.info("=== Token Generated: ====================================")
            GCLogger.info("    $token")
            GCLogger.info("=========================================================")
            tokens.add(token)
            tokensGenerated = true
        }
        if (tokensGenerated) GCLogger.info("TOKEN(S) WILL NOT BE SHOWN AGAIN")
    }

    suspend fun create(source: String = "API"): String = dbQuery {
        val generatedToken = generateSecureTokenString(32)
        Tokens.insertAndGetId {
            it[token] = generatedToken
            it[generatedAt] = Instant.now()
                .atZone(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            it[createdBy] = source
        }
        generatedToken
    }

    suspend fun getAllTokens(): MutableList<String>{
        return dbQuery {
            Tokens
                .select(Tokens.token)
                .map { it[Tokens.token]}.toMutableList()

        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            Tokens.deleteWhere { Tokens.id.eq(id) }
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    private fun generateSecureTokenString(length: Int): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = SecureRandom()
        return (1..length)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }
}


