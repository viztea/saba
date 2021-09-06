package lol.saba.server.http

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import lol.saba.server.Saba
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

class HttpManager : CoroutineScope {
    companion object {
        val logger = LoggerFactory.getLogger(HttpManager::class.java)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + Job()

    val discordRedirectUri =
        "http%3A%2F%2F${Saba.config.getString("host")}%3A6610%2Fdiscord%2Fcallback"

    val redirectUrl =
        "https://discord.com/api/oauth2/authorize?client_id=884188784856559656&redirect_uri=$discordRedirectUri&response_type=code&scope=identify"

    val client = HttpClient(io.ktor.client.engine.cio.CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    fun listen() {
        val server = embeddedServer(CIO, port = 6610) {
            install(DefaultHeaders) {
                header("Server", "Saba-Server")
                header("X-Powered-By", "tcp sockets uwu")
            }

            install(ContentNegotiation) {
                json(Json)
            }

            routing {
                get("/discord/login") {
                    call.respondRedirect(redirectUrl)
                }

                get("/discord/callback") {
                    val code = call.parameters["code"]
                        ?: return@get call.respond(mapOf("message" to "missing 'code' query parameter."))

                    val json = client.submitForm<JsonObject>(
                        url = "https://discord.com/api/v9/oauth2/token",
                        formParameters = Parameters.build {
                            append("client_id", Saba.config.getString("client-id"))
                            append("client_secret", Saba.config.getString("client-secret"))
                            append("grant_type", "authorization_code")
                            append("redirect_uri", "http://${Saba.config.getString("host")}:6610/discord/callback")
                            append("code", code)
                        }
                    )

                    call.respondRedirect("http://localhost:6611/?token=${json["access_token"]?.jsonPrimitive?.content}")
                }
            }
        }

        server.start()
    }
}
