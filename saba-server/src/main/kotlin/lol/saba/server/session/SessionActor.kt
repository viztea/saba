package lol.saba.server.session

import lol.saba.common.ActorStatus
import lol.saba.common.entity.Actor
import lol.saba.common.messages.SabaMessage
import lol.saba.server.clients.SabaClient
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class SessionActor(val session: SabaSession, val userId: Long) {

    /**
     * The client can be null since reconnecting is a possibility
     */
    var client: SabaClient? = null
    var status: ActorStatus = ActorStatus.Idle

    val info: Actor
        get() = Actor(userId, client?.id!!)

    private val queue: Queue<SabaMessage.RTS> = ConcurrentLinkedQueue()

    suspend fun send(message: SabaMessage.RTS): Boolean {
        return client?.send(message) ?: queue.offer(message)
    }

    suspend fun flushQueue() {
        while (true) {
            val message = queue.peek()
                ?: break

            client?.send(message)
                ?.let { queue.poll() }
                ?: continue
        }
    }
}
