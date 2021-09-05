package lol.saba.common.messages

import lol.saba.common.messages.impl.Close
import lol.saba.common.messages.impl.Identify
import kotlin.reflect.KClass

enum class MessageDirection(vararg val messages: Pair<Int, KClass<out SabaMessage>>) {
    C2S(0 to Close::class, 1 to Identify::class, 2 to Identify.Actor::class),
    S2C(0 to Close::class);

    companion object {
        fun find(direction: MessageDirection, klass: KClass<out SabaMessage>) =
            direction.messages.find { (_, it) -> it == klass }

        fun find(direction: MessageDirection, id: Int) =
            direction.messages.find { (it) -> it == id }
    }
}
