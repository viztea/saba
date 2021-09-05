package lol.saba.server.networking.message

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import lol.saba.server.networking.message.impl.Close
import kotlin.reflect.KClass

data class MessageDescription(val klass: KClass<out SabaMessage>) {
    @OptIn(InternalSerializationApi::class)
    val serializer get() = klass.serializer()
}

fun KClass<out SabaMessage>.description() = MessageDescription(this)

enum class MessageDirection { C2S, S2C }

object SabaMessages {
    val fromClient = mapOf(
        0 to Close::class.description(),
        1 to Identify::class.description(),
        2 to IdentifyActor::class.description(),
        3 to IdentifyDirector::class.description()
    )

    val toClient = mapOf(
        0 to Close::class.description()
    )

    fun find(direction: MessageDirection, klass: KClass<out SabaMessage>) = when (direction) {
        MessageDirection.C2S -> fromClient.entries.find { (_, it) -> it.klass == klass }
        MessageDirection.S2C -> toClient.entries.find { (_, it) -> it.klass == klass }
    }

    fun find(direction: MessageDirection, id: Int): MessageDescription? = when (direction) {
        MessageDirection.C2S -> fromClient[id]
        MessageDirection.S2C -> toClient[id]
    }
}
