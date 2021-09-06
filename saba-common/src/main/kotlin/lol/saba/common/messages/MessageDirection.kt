package lol.saba.common.messages

import lol.saba.common.messages.bi.Close
import lol.saba.common.messages.c2s.Identify
import lol.saba.common.messages.c2s.UpdateActorStatus
import lol.saba.common.messages.s2c.ClientMetadata
import lol.saba.common.messages.bi.Play
import lol.saba.common.messages.s2c.SessionMetadata
import lol.saba.common.messages.bi.Stop
import lol.saba.common.messages.c2s.CreateSession
import lol.saba.common.messages.c2s.JoinSession
import kotlin.reflect.KClass

enum class MessageDirection(vararg val messages: Pair<Int, KClass<out SabaMessage>>) {
    C2S(
        0 to Close::class,
        1 to Identify::class,
        2 to Identify.Actor::class,
        3 to UpdateActorStatus::class,
        4 to Stop::class,
        5 to Play::class,
        6 to CreateSession::class,
        7 to JoinSession::class),

    S2C(
        0 to Close::class,
        1 to ClientMetadata::class,
        2 to SessionMetadata::class,
        3 to Stop::class,
        4 to Play::class);

    companion object {
        fun find(direction: MessageDirection, klass: KClass<out SabaMessage>) =
            direction.messages.find { (_, it) -> it == klass }

        fun find(direction: MessageDirection, id: Int) =
            direction.messages.find { (it) -> it == id }
    }
}
