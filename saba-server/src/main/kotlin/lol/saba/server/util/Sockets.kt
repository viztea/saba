package lol.saba.server.util

import io.ktor.utils.io.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import lol.saba.server.networking.message.*

object Sockets {
    val protobuf = ProtoBuf {  }

    suspend fun <T : SabaMessage> write(writeChannel: ByteWriteChannel, direction: MessageDirection, message: T) {
        val (id, type) = SabaMessages.find(direction, message::class)
            ?: throw error("Unknown message type.")

        val packet = protobuf.encodeToByteArray(type.serializer as SerializationStrategy<T>, message)

        /* write the packet. */
        writeChannel.writeInt(id)
        writeChannel.writeInt(packet.size)
        writeChannel.writeFully(packet)
    }

    suspend inline fun <reified T : SabaMessage> read(readChannel: ByteReadChannel): T {
        return read(readChannel, T::class.description())
    }

    suspend inline fun <T : SabaMessage> read(readChannel: ByteReadChannel, description: MessageDescription): T {
        readChannel.readInt()

        /* read the packet length. */
        val length = readChannel.readInt()

        /* read the packet. */
        val packet = ByteArray(length)
        readChannel.readFully(packet)

        /* deserialize the packet. */
        return protobuf.decodeFromByteArray(description.serializer as DeserializationStrategy<T>, packet)
    }

    suspend fun read(readChannel: ByteReadChannel, direction: MessageDirection): SabaMessage? {
        /* read the id of the packet. */
        val id = readChannel.readInt()

        /* read the packet length. */
        val length = readChannel.readInt()

        /* read the packet. */
        val packet = ByteArray(length)
        readChannel.readFully(packet)

        /* deserialize the packet. */
        val messageDescription = SabaMessages.find(direction, id)
            ?: return null

        return protobuf.decodeFromByteArray(messageDescription.serializer, packet)
    }
}
