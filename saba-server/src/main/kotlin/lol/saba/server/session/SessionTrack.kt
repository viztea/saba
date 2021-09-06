package lol.saba.server.session

import lol.saba.common.entity.Track
import lol.saba.common.messages.bi.Play
import lol.saba.common.messages.bi.Stop

class SessionTrack(val session: SabaSession) {
    var paused: Boolean = false

    var encoded: String? = null

    var playingTimestamp: Long = -1

    val position: Long
        get() = if (playingTimestamp == -1L) -1 else System.currentTimeMillis() - playingTimestamp

    val info: Track
        get() = Track(encoded!!, position, paused)

    suspend fun stop() {
        encoded = null
        playingTimestamp = -1
        updateActors()
        session.updateDirector()
    }

    suspend fun updateActors() {
        // session id is redundant since actors may only be in one session at once.
        val message = if (encoded == null) Stop(session.id) else Play(encoded!!, session.id)
        session.broadcast(message)
    }
}
