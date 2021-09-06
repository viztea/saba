package lol.saba.common.messages.s2c

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import lol.saba.common.entity.Session
import lol.saba.common.messages.SabaMessage
import java.util.*

@Serializable
data class SessionMetadata(
    @Contextual val id: UUID,
    val info: Session
) : SabaMessage
