import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import lol.saba.server.networking.message.ClientRole
import lol.saba.server.networking.message.Identify
import lol.saba.server.networking.message.MessageDirection
import lol.saba.server.networking.message.impl.Close
import lol.saba.server.util.Sockets

suspend fun main() {
    val client = aSocket(ActorSelectorManager(Dispatchers.IO))
        .tcp()
        .connect(hostname = "127.0.0.1", port = 6999)

    val writeChannel = client.openWriteChannel(true)
    Sockets.write(writeChannel, MessageDirection.C2S, Identify(ClientRole.Director))
//    Sockets.write(writeChannel, MessageDirection.C2S, Close(420, "cus i said so"))
    client.dispose()
    client.awaitClosed()
}
