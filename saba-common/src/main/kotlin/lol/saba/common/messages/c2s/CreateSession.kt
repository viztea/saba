package lol.saba.common.messages.c2s

import kotlinx.serialization.Serializable
import lol.saba.common.messages.SabaMessage

@Serializable
data class CreateSession(val guild: Long) : SabaMessage.FromDirector
