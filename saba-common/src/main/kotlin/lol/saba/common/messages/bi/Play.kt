package lol.saba.common.messages.bi

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import lol.saba.common.messages.SabaMessage
import java.util.*

@Serializable
data class Play(val track: String, @Contextual val session: UUID) : SabaMessage.RTS, SabaMessage.ToActor, SabaMessage.FromDirector
