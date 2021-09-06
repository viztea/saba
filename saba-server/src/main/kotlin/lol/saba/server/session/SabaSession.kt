package lol.saba.server.session

import lol.saba.common.entity.Session
import lol.saba.common.messages.SabaMessage
import lol.saba.common.messages.s2c.SessionMetadata
import lol.saba.server.Saba
import lol.saba.server.clients.SabaClient
import org.slf4j.LoggerFactory
import java.util.*

class SabaSession(val id: UUID, val director: UUID, val guild: Long) {
    companion object {
        private val logger = LoggerFactory.getLogger(SabaSession::class.java)
    }

    val actors: MutableMap<Long, SessionActor> = mutableMapOf()
    var track: SessionTrack = SessionTrack(this)

    val metadata: SessionMetadata
        get() = SessionMetadata(id, info)

    val info: Session
        get() = Session(guild, actors.values.map { it.info }, id = id, currentTrack = if (track.encoded == null) null else track.info)

    suspend fun play(track: String) {
        logger.info("Director $director is playing track $track")
        this.track.encoded = track
        this.track.playingTimestamp = System.currentTimeMillis()
        this.track.updateActors()
        updateDirector()
    }

    suspend fun broadcast(message: SabaMessage.RTS) {
        actors.forEach { (_, actor) -> actor.send(message) }
    }

    suspend fun addActor(client: SabaClient, userId: Long) {
        if (actors.contains(userId)) {
            return updateActor(client, userId)
        }

        val actor = SessionActor(this, userId)
        actor.client = client
        actors[userId] = actor

        updateDirector()
        sendMetadata(client)
    }

    suspend fun updateActor(client: SabaClient, userId: Long) {
        val actor = actors[userId]
            ?: return addActor(client, userId)

        logger.info("Updating actor $userId")
        actor.client = client

        sendMetadata(client)
        updateDirector()
        actor.flushQueue()
    }

    suspend fun updateDirector() {
        Saba.clients[director]?.send(metadata)
    }

    suspend fun sendMetadata(client: SabaClient) =
        client.send(metadata)
}
