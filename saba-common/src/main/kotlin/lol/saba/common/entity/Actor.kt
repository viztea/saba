package lol.saba.common.entity

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Actor(val userId: Long, @Contextual val clientId: UUID)
