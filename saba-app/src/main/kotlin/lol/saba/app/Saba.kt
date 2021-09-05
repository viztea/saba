package lol.saba.app

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import lol.saba.common.ClientRole
import lol.saba.common.messages.MessageDirection
import lol.saba.common.messages.MessageUtil
import lol.saba.common.messages.impl.Close
import lol.saba.common.messages.impl.Identify
import org.slf4j.LoggerFactory

object Saba {
    private val logger = LoggerFactory.getLogger(Saba::class.java)

    @JvmStatic
    fun main(args: Array<out String>) = runBlocking { start() }

    suspend fun start() {
        val client = aSocket(ActorSelectorManager(Dispatchers.IO))
            .tcp()
            .connect(hostname = "127.0.0.1", port = 6999)

        logger.info("Connected to saba server.")

        val writeChannel = client.openWriteChannel(true)

        logger.info("Identifying as a director.")
        MessageUtil.write(writeChannel, MessageDirection.C2S, Identify(ClientRole.Director))

        logger.info("Closing...")
        MessageUtil.write(writeChannel, MessageDirection.C2S, Close(420, "cus i said so"))
        client.dispose()
        client.awaitClosed()
    }
}
