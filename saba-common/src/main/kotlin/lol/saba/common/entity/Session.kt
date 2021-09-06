package lol.saba.common.entity

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Session(val guild: Long, val actors: List<Actor>, val currentTrack: Track?, @Contextual val id: UUID)
