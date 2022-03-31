package lol.saba.common.messages

import io.ktor.utils.io.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.protobuf.ProtoBuf
import java.util.*

@OptIn(InternalSerializationApi::class)
object MessageUtil {
    val protobuf = ProtoBuf {
        serializersModule = SerializersModule {
            contextual(UUIDSerializer)
        }
    }

    suspend fun <T : SabaMessage> write(writeChannel: ByteWriteChannel, direction: MessageDirection, message: T) {
        val (id, type) = MessageDirection.find(direction, message::class)
            ?: error("Unknown message type.")

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

    object UUIDSerializer : KSerializer<UUID> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("java.util.UUID", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): UUID {
            val stringifiedUUID = decoder.decodeString()
            return UUID.fromString(stringifiedUUID)
        }

        override fun serialize(encoder: Encoder, value: UUID) {
            encoder.encodeString(value.toString())
        }
    }
}
