package lol.saba.server.networking.message.impl

import kotlinx.serialization.Serializable
import lol.saba.server.networking.message.SabaMessage

@Serializable
data class Close(val code: Int, val reason: String) : SabaMessage
