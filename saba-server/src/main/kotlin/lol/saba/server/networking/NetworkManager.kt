package lol.saba.server.networking

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import lol.saba.common.ClientRole
import lol.saba.common.messages.impl.Identify
import lol.saba.common.extensions.read
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class NetworkManager : CoroutineScope {
    companion object {
        val logger = LoggerFactory.getLogger(NetworkManager::class.java)
    }

    private val dispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()
    private val server: ServerSocket = aSocket(ActorSelectorManager(dispatcher))
        .tcp()
        .bind(port = 6999)

    override val coroutineContext: CoroutineContext
        get() = dispatcher + SupervisorJob()

    fun listen() = runBlocking {
        logger.info("Now listening on port 6999")
        while (true) {
            val socket = server.accept()
            logger.debug("Client has connected from ${socket.remoteAddress}")
            launch {
                try {
                    val readChannel = socket.openReadChannel()

                    /* identify. */
                    readChannel.awaitContent()
                    val identify = withTimeoutOrNull(5000L) { readChannel.read<Identify>() }
                    if (identify is Identify) {
                        val client = when (identify.role) {
                            ClientRole.Actor -> {
                                val roleInfo = readChannel.read<Identify.Actor>()
                                SabaClient(socket, readChannel, roleInfo.userId)
                            }

                            ClientRole.Director -> SabaClient(socket, readChannel, null)
                        }

                        logger.info("Client connected as ${identify.role.name}")
                        launch {
                            client.listen()
                        }
                    } else {
                        logger.warn("Did not receive identify message in time.")
                        socket.dispose()
                    }
                } catch (e: ClosedReceiveChannelException) {
                    logger.warn("Client ${socket.remoteAddress} had a pre-mature close...")
                } catch (e: Exception) {
                    logger.error("Error occurred while listening to client ${socket.remoteAddress}", e)
                }
            }
        }
    }
}
