package lol.saba.common.messages.c2s

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import lol.saba.common.messages.SabaMessage
import java.util.*

@Serializable
data class JoinSession(val userId: Long, @Contextual val session: UUID) : SabaMessage.FromDirector
