package lol.saba.common.messages.c2s

import kotlinx.serialization.Serializable
import lol.saba.common.ActorStatus
import lol.saba.common.messages.SabaMessage

@Serializable
data class UpdateActorStatus(val status: ActorStatus) : SabaMessage.RTS, SabaMessage.FromActor
