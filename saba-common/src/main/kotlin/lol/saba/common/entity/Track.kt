package lol.saba.common.entity

import kotlinx.serialization.Serializable

@Serializable
data class Track(val encoded: String, val position: Long, val paused: Boolean)
