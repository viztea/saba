package lol.saba.app.util

import androidx.compose.ui.graphics.ImageBitmap
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.http.*
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.request.*
import org.jetbrains.skia.Image

object HttpTools {
    val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
            defaultRequest {
                userAgent("SabaApp (https://github.com/melike2d/saba)")
            }
        }
    }

    suspend fun loadPicture(url: String): ImageBitmap {
        val image = client.get<ByteArray>(url)

        return Image.makeFromEncoded(image).toComposeImageBitmap()
    }
}
