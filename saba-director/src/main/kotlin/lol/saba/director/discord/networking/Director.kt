package lol.saba.director.discord.networking

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import lol.saba.common.ClientRole
import lol.saba.common.entity.Session
import lol.saba.common.extensions.read
import lol.saba.common.extensions.write
import lol.saba.common.messages.MessageDirection
import lol.saba.common.messages.SabaMessage
import lol.saba.common.messages.bi.Close
import lol.saba.common.messages.c2s.Identify
import lol.saba.common.messages.s2c.SessionMetadata
import lol.saba.director.discord.Bot
import lol.saba.director.discord.extensions.on
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext

class Director : CoroutineScope {
    companion object {
        private val logger = LoggerFactory.getLogger(Director::class.java)
    }

    lateinit var socket: Socket

    val readChannel by lazy { socket.openReadChannel() }
    val writeChannel by lazy { socket.openWriteChannel(true) }

    val events: SharedFlow<SabaMessage>
        get() = eventFlow

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + Job()

    private val queue = ConcurrentLinkedQueue<SabaMessage>()
    private val eventFlow = MutableSharedFlow<SabaMessage>(extraBufferCapacity = Int.MAX_VALUE)

    init {
        /* listen for session updates. */
        on<SessionMetadata> {
            val session = Bot.sessions[it.info.guild]
            if (session == null) {
                Bot.sessions[it.info.guild] = SabaSession(it.info.guild, it.info, this)
            } else {
                session.info = it.info
            }
        }
    }

    suspend fun connect() {
        socket = aSocket(ActorSelectorManager(Dispatchers.IO))
            .tcp()
            .connect(hostname = Bot.config.getString("host"), port = 6609)

        logger.info("Connected to Saba server")

        /* identify as a director. */
        send(Identify(ClientRole.Director))

        /* listen for events. */
        listen()
    }

    suspend fun send(message: SabaMessage): Boolean {
        if (writeChannel.isClosedForWrite) {
            return queue.offer(message)
        }

        return message
            .runCatching { writeChannel.write(MessageDirection.C2S, this) }
            .onFailure { logger.error("Failed to send message", it) }
            .isSuccess
    }

    suspend fun close(reason: String, code: Int = 1000) {
        val close = Close(code, reason)
        send(close)
        handleClose(close, false)
    }

    private fun listen() = launch {
        try {
            var close: Close? = null
            while (!readChannel.isClosedForRead) {
                readChannel.awaitContent()
                val message = readChannel.read(MessageDirection.S2C)
                    ?: continue

                if (message is Close) {
                    close = message
                    break
                }

                eventFlow.emit(message)
            }

            handleClose(close, true)
        } catch (e: ClosedReceiveChannelException) {
            handleClose(Close(-1, "Receive channel has closed"), false)
        } catch (e: Exception) {
            logger.error("Error occurred", e)
            if (!socket.isClosed) {
                handleClose(Close(1000, e.message ?: e::class.simpleName ?: "unknown cause"), false)
            }
        }
    }

    private fun handleClose(close: Close?, remote: Boolean) {
        if (close == null) {
            logger.warn("Closing without reason...")
        }

        logger.info("Closing... remote=$remote; reason=${close?.reason}; code=${close?.code}")
        socket.dispose()
    }
}
