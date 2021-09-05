package lol.saba.common.messages.impl

import kotlinx.serialization.Serializable
import lol.saba.common.messages.SabaMessage

@Serializable
data class Close(val code: Int, val reason: String) : SabaMessage
