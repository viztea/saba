package lol.saba.server.clients

import io.ktor.network.sockets.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ClientManager(private val clients: ConcurrentHashMap<UUID, SabaClient> = ConcurrentHashMap()) : Map<UUID, SabaClient> by clients {
    suspend fun create(socket: Socket): SabaClient? {
        val client = SabaClient(UUID.randomUUID(), socket)
        if (!client.discoverRole()) {
            return null
        }

        clients[client.id] = client
        return client
    }

    fun findByUserId(userId: Long): SabaClient? = values.find { it.userId == userId }
}
