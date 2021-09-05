package lol.saba.server.networking

import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import lol.saba.common.messages.MessageDirection
import lol.saba.common.messages.SabaMessage
import lol.saba.common.messages.impl.Close
import lol.saba.common.extensions.read
import lol.saba.common.extensions.write
import org.slf4j.LoggerFactory

class SabaClient(val socket: Socket, val readChannel: ByteReadChannel, val userId: Long?) {
    companion object {
        private val logger = LoggerFactory.getLogger(SabaClient::class.java)
    }

    val writeChannel: ByteWriteChannel by lazy {
        socket.openWriteChannel(true)
    }

    val isActor: Boolean
        get() = userId != null

    val isDirector: Boolean
        get() = !isActor

    suspend fun send(message: SabaMessage): Boolean {
        if (writeChannel.isClosedForWrite) {
            return false
        }

        logger.debug("Sending ${message::class.qualifiedName} to client ${socket.remoteAddress}")
        writeChannel.write(MessageDirection.S2C, message)
        return true
    }

    suspend fun close(reason: String, code: Int) {
        val closeReason = Close(code, reason)
        send(closeReason)
        handleClose(closeReason, false)
    }

    suspend fun listen() {
        var closeReason: Close? = null
        while (!readChannel.isClosedForRead) {
            readChannel.awaitContent()
            val message = readChannel.read(MessageDirection.C2S)
            if (message is Close) {
                closeReason = message
                break
            }

            println(message)
        }

        handleClose(closeReason, true)
    }

    private fun handleClose(closeReason: Close?, remote: Boolean) {
        if (closeReason == null) {
            logger.warn("Closing without reason.")
        }

        logger.debug("Client has disconnected, remote=$remote; reason=${closeReason?.reason}; code=${closeReason?.code}.")
        socket.dispose()
    }
}
