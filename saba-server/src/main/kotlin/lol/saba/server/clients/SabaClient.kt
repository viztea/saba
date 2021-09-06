package lol.saba.server.clients

import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withTimeoutOrNull
import lol.saba.common.ClientRole
import lol.saba.common.extensions.read
import lol.saba.common.extensions.write
import lol.saba.common.messages.MessageDirection
import lol.saba.common.messages.SabaMessage
import lol.saba.common.messages.bi.Close
import lol.saba.common.messages.bi.Play
import lol.saba.common.messages.bi.Stop
import lol.saba.common.messages.c2s.CreateSession
import lol.saba.common.messages.c2s.Identify
import lol.saba.common.messages.c2s.JoinSession
import lol.saba.common.messages.s2c.ClientMetadata
import lol.saba.server.NetworkManager
import lol.saba.server.Saba
import lol.saba.server.extensions.on
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.coroutines.CoroutineContext

class SabaClient(val id: UUID, val socket: Socket) : CoroutineScope {
    companion object {
        private val logger = LoggerFactory.getLogger(SabaClient::class.java)
    }

    lateinit var role: ClientRole

    var userId: Long? = null
        private set

    val readChannel = socket.openReadChannel()
    val writeChannel = socket.openWriteChannel(true)

    val events: SharedFlow<SabaMessage>
        get() = eventFlow

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + Job()

    private val eventFlow = MutableSharedFlow<SabaMessage>(extraBufferCapacity = Int.MAX_VALUE)

    suspend fun discoverRole(): Boolean {
        readChannel.awaitContent()
        val identify = withTimeoutOrNull(5000L) {
            readChannel.read<Identify>(false)
        }

        if (identify == null) {
            logger.warn("Did not receive identify message in time.")
            close("Did not receive identify in time", 4000)
            return false
        }

        role = identify.role
        if (role == ClientRole.Actor) {
            val (userId) = readChannel.read<Identify.Actor>()
            this.userId = userId
        }

        init()
        return true
    }

    suspend fun init() {
        logger.info("Client $id has indentified as ${role.name}")
        if (role == ClientRole.Director) {
            on<CreateSession> {
                println(Saba.sessions
                    .create(id, it.guild)
                    .sendMetadata(this))
            }

            on<JoinSession> {
                val client = Saba.clients.findByUserId(it.userId)
                if (client == null) {
                    logger.warn("Director $id wanted to make unknown user ${it.userId} join session ${it.session}")
                    return@on close("Unknown Session", 4004)
                }

                val session = Saba.sessions[it.session]
                if (session == null) {
                    logger.warn("Director $id wanted to play a track in an unknown session")
                    return@on close("Unknown Session", 4004)
                }

                session.addActor(client, it.userId)
            }

            on<Play> {
                val session = Saba.sessions[it.session]
                if (session == null) {
                    logger.warn("Director $id wanted to play a track in an unknown session")
                    return@on close("Unknown Session", 4004)
                }

                session.play(it.track)
            }

            on<Stop> {
                val session = Saba.sessions[it.session]
                if (session == null) {
                    logger.warn("Director $id wanted to play a track in an unknown session")
                    return@on close("Unknown Session", 4004)
                }

                session.track.stop()
            }
        }
    }

    suspend fun findOldSession() {
        val session = Saba.sessions.values.find { session ->
            session.actors.any { (_, it) -> it.userId == userId }
        }

        if (session != null) {
            val metadata = ClientMetadata(role, id, session.id)
            send(metadata)
            session.updateActor(this, userId!!)
        }
    }

    suspend fun send(message: SabaMessage): Boolean {
        if (writeChannel.isClosedForWrite) {
            return false
        }

        logger.debug("Client $id <<< ${message::class.qualifiedName}")
        writeChannel.write(MessageDirection.S2C, message)
        return true
    }

    suspend fun close(reason: String, code: Int) {
        val close = Close(code, reason)
        send(close)
        handleClose(close, false)
    }

    suspend fun listen() {
        var close: Close? = null
        while (!readChannel.isClosedForRead) {
            readChannel.awaitContent()
            val message = readChannel.read(MessageDirection.C2S)
                ?: continue

            if (message is Close) {
                close = message
                break
            }

            eventFlow.emit(message)
        }

        handleClose(close, true)
    }

    private fun handleClose(close: Close?, remote: Boolean) {
        if (close == null) {
            logger.warn("Client $id is without a reason.")
        }

        logger.info("Client $id has disconnected, remote=$remote; reason=${close?.reason}; code=${close?.code}.")
        socket.dispose()
    }
}
