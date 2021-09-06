package lol.saba.common.extensions

import io.ktor.utils.io.*
import lol.saba.common.messages.MessageDirection
import lol.saba.common.messages.MessageUtil
import lol.saba.common.messages.SabaMessage

suspend inline fun <reified T : SabaMessage> ByteReadChannel.read(awaitContent: Boolean = true): T {
    if (awaitContent) {
        awaitContent()
    }

    return MessageUtil.read(this)
}

suspend fun ByteReadChannel.read(direction: MessageDirection, awaitContent: Boolean = true): SabaMessage? {
    if (awaitContent) {
        awaitContent()
    }

    return MessageUtil.read(this, direction)
}

suspend fun ByteWriteChannel.write(direction: MessageDirection, message: SabaMessage) {
    MessageUtil.write(this, direction, message)
}
