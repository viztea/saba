package lol.saba.server

import lol.saba.server.networking.NetworkManager

object Saba {
    val networking = NetworkManager()

    @JvmStatic
    fun main(args: Array<out String>) = listen()

    fun listen() {
        networking.listen()
    }
}
