package lol.saba.server

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import lol.saba.common.extensions.read
import lol.saba.common.messages.c2s.Identify
import lol.saba.server.clients.SabaClient
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

class NetworkManager : CoroutineScope {
    companion object {
        val logger = LoggerFactory.getLogger(NetworkManager::class.java)
    }

    val port = 6609

    private val server: ServerSocket = aSocket(ActorSelectorManager(Dispatchers.IO))
        .tcp()
        .bind(port = port)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + SupervisorJob()

    fun listen() = runBlocking {
        logger.info("Saba has started listening on port $port")
        while (true) {
            val socket = server.accept()
            logger.info("Client has connected from ${socket.remoteAddress}")
            launch {
                try {
                    val client = Saba.clients.create(socket)
                        ?: return@launch cancel()

                    client.listen()
                } catch (e: ClosedReceiveChannelException) {
                    logger.warn("Client ${socket.remoteAddress} had a pre-mature close...")
                } catch (e: Exception) {
                    logger.error("Error occurred while listening to client ${socket.remoteAddress}", e)
                }
            }
        }
    }
}
