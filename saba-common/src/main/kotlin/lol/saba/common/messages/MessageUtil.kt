package lol.saba.common.messages

import io.ktor.utils.io.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer

@OptIn(InternalSerializationApi::class)
object MessageUtil {
    val protobuf = ProtoBuf {  }

    suspend fun <T : SabaMessage> write(writeChannel: ByteWriteChannel, direction: MessageDirection, message: T) {
        val (id, type) = MessageDirection.find(direction, message::class)
            ?: throw error("Unknown message type.")

        val packet = protobuf.encodeToByteArray(type.serializer() as SerializationStrategy<T>, message)

        /* write the packet. */
        writeChannel.writeInt(id)
        writeChannel.writeInt(packet.size)
        writeChannel.writeFully(packet)
    }

    suspend inline fun <reified T : SabaMessage> read(readChannel: ByteReadChannel): T {
        readChannel.readInt()

        /* read the packet length. */
        val length = readChannel.readInt()

        /* read the packet. */
        val packet = ByteArray(length)
        readChannel.readFully(packet)

        /* deserialize the packet. */
        return protobuf.decodeFromByteArray(T::class.serializer() as DeserializationStrategy<T>, packet)
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
        val (_, kClass) = MessageDirection.find(direction, id)
            ?: return null

        return protobuf.decodeFromByteArray(kClass.serializer(), packet)
    }
}
