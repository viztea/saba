package lol.saba.server.networking.message

import kotlinx.serialization.Serializable

enum class ClientRole {
    /**
     * Listens to directions from the director.
     */
    Actor,

    /**
     * This client directs all actors.
     */
    Director
}

@Serializable
data class Identify(val role: ClientRole) : SabaMessage

@Serializable
data class IdentifyActor(val userId: Long) : SabaMessage

@Serializable
object IdentifyDirector : SabaMessage
