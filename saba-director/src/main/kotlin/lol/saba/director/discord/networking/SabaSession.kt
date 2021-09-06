package lol.saba.director.discord.networking

import lavaplayer.track.AudioTrack
import lol.saba.common.entity.Session
import lol.saba.common.messages.bi.Play
import lol.saba.director.discord.Bot
import java.util.*

class SabaSession(val guild: Long, var info: Session, val director: Director) {
    val id: UUID
        get() = info.id

    suspend fun play(track: AudioTrack) {
        play(Bot.trackUtil.encodeTrack(track))
    }

    suspend fun play(track: String) {
        val play = Play(track, id)
        director.send(play)
    }
}
