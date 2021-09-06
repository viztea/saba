package lol.saba.server

import lol.saba.server.clients.ClientManager
import lol.saba.server.http.HttpManager
import lol.saba.server.session.SessionManager

object Saba {
    val networking = NetworkManager()
    val clients = ClientManager()
    val sessions = SessionManager()
    val http = HttpManager()

    @JvmStatic
    fun main(args: Array<out String>) = listen()

    fun listen() {
        http.listen()
        networking.listen()
    }
}
