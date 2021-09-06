package lol.saba.server

import com.typesafe.config.ConfigFactory
import lol.saba.server.clients.ClientManager
import lol.saba.server.http.HttpManager
import lol.saba.server.session.SessionManager

object Saba {
    val config = ConfigFactory.load().getConfig("saba")
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
