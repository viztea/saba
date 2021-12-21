package lol.saba.app.util

import androidx.compose.ui.unit.dp
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer
import lol.saba.app.SabaApp
import spark.kotlin.get
import spark.kotlin.port

class Discord(val saba: SabaApp) {
    val redirectUri: String
        get() = "http://${saba.config.getString("server.host")}:${saba.config.getInt("server.http-port")}/discord/login"

    var accessToken: String? = saba.preferences.get("accessToken", null)

    init {
        port(6611)

        get("/") {
            accessToken = queryParams("token")

            saba.preferences.put("accessToken", accessToken)
            runBlocking {
                saba.onAuthenticated()
            }

            "thanks bro, you can close this page now."
        }
    }

    suspend fun getSelf(): DiscordUser? {
        val accessToken = accessToken
            ?: return null

        val response = HttpTools.client.get<HttpResponse>("https://discord.com/api/v9/users/@me") {
            header("Authorization", "Bearer $accessToken")
        }

        return when (response.status) {
            HttpStatusCode.OK -> response.receive()

            HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> null

            else -> error("Can't make request to discord lol")
        }
    }

    @Serializable
    data class DiscordUser(@Serializable(with = LongAsStringSerializer::class) val id: Long, val username: String, val discriminator: String, val avatar: String?) {
        companion object {
            val IMAGE_SIZE = 48.dp
        }

        val tag: String
            get() = "$username#$discriminator"

        val avatarUrl: String?
            get() = avatar?.let { hash -> "https://cdn.discordapp.com/avatars/$id/$hash.png?size=$IMAGE_SIZE" }
    }
}
