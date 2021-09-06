package lol.saba.common.messages.s2c

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import lol.saba.common.ClientRole
import lol.saba.common.messages.SabaMessage
import java.util.*

@Serializable
data class ClientMetadata(val role: ClientRole, @Contextual val id: UUID, @Contextual val session: UUID) : SabaMessage
