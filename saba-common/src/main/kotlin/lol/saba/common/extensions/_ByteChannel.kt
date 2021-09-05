package lol.saba.common.extensions

import io.ktor.utils.io.*
import lol.saba.common.messages.MessageDirection
import lol.saba.common.messages.MessageUtil
import lol.saba.common.messages.SabaMessage

suspend inline fun <reified T : SabaMessage> ByteReadChannel.read(): T {
    return MessageUtil.read(this)
}

suspend fun ByteReadChannel.read(direction: MessageDirection): SabaMessage? {
    return MessageUtil.read(this, direction)
}

suspend fun ByteWriteChannel.write(direction: MessageDirection, message: SabaMessage) {
    MessageUtil.write(this, direction, message)
}
