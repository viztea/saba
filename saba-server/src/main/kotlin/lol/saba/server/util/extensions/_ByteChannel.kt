package lol.saba.server.util.extensions

import io.ktor.utils.io.*
import lol.saba.server.networking.message.MessageDirection
import lol.saba.server.networking.message.SabaMessage
import lol.saba.server.util.Sockets

suspend inline fun <reified T : SabaMessage> ByteReadChannel.read(): T {
    return Sockets.read(this)
}

suspend fun ByteReadChannel.read(direction: MessageDirection): SabaMessage? {
    return Sockets.read(this, direction)
}

suspend fun ByteWriteChannel.write(direction: MessageDirection, message: SabaMessage) {
    Sockets.write(this, direction, message)
}
