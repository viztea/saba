package lol.saba.app.util

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer
import kotlinx.serialization.json.Json as Kotlinx
import lol.saba.app.SabaApp
import spark.kotlin.get
import spark.kotlin.port
import spark.kotlin.stop

class Discord(val saba: SabaApp) {
    private val redirectUri = "http://${saba.config.getString("server.host")}:${saba.config.getInt("server.http-port")}/discord/login"

    var accessToken: String? = saba.preferences.get("accessToken", null)
    val httpClient = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Kotlinx { ignoreUnknownKeys = true })
            defaultRequest {
                userAgent("SabaApp (https://github.com/melike2d/saba)")
            }
        }
    }

    suspend fun doOauth(): String {
        if (accessToken != null) {
            return accessToken!!
        }

        val deferred = CompletableDeferred<String>()

        port(6611)
        get("/") {
            accessToken = queryParams("token")
            saba.preferences.put("accessToken", accessToken)
            deferred.complete(accessToken!!)
            "thanks bro, you can close this page now."
        }

        deferred.invokeOnCompletion {
            stop()
        }

        saba.hostServices.showDocument(redirectUri)
        return deferred.await()
    }

    suspend fun getSelf(): DiscordUser {
        val accessToken = accessToken ?: doOauth()
        return httpClient.get("https://discord.com/api/v9/users/@me") {
            header("Authorization", "Bearer $accessToken")
        }
    }

    @Serializable
    data class DiscordUser(@Serializable(with = LongAsStringSerializer::class) val id: Long, val username: String, val discriminator: String, val avatar: String?)
}
