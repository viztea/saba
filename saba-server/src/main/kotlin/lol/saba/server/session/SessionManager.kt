package lol.saba.server.session

import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SessionManager(val sessions: ConcurrentHashMap<UUID, SabaSession> = ConcurrentHashMap()) : Map<UUID, SabaSession> by sessions {
    companion object {
        private val logger = LoggerFactory.getLogger(SessionManager::class.java)
    }

    fun create(director: UUID, guild: Long): SabaSession {
        logger.info("Director $director is creating session for guild $guild")

        val session = SabaSession(UUID.randomUUID(), director, guild)
        sessions[session.id] = session

        return session
    }
}
