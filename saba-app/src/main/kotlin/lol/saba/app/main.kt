package lol.saba.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.runBlocking
import lol.saba.app.util.Discord.DiscordUser.Companion.IMAGE_SIZE
import lol.saba.app.util.HttpTools
import java.awt.Desktop
import java.net.URI
import kotlin.math.roundToInt

suspend fun main() = application {
    fun close() {
        exitApplication()
    }

    /* application state */
    val nowPlaying = remember { mutableStateOf("nothing") }
    val volume = remember { mutableStateOf(1f) }
    val loading = remember { mutableStateOf(true) }

    /* saba */
    val saba = SabaApp(nowPlaying, volume, loading)
    runBlocking {
        saba.start()
    }

    /* ui */
    val windowState = rememberWindowState(width = 300.dp, height = 300.dp)
    Window(
        title = "Saba",
        state = windowState,
        onCloseRequest = ::close
    ) {
        if (!loading.value) {
            Column {
                Box(
                    modifier = Modifier
                        .size(width = windowState.size.width, height = 72.dp)
                        .fillMaxSize()
                        .background(Color(122, 255, 175))
                        .padding(10.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        saba.self.avatarUrl?.let {
                            Image(
                                bitmap = runBlocking { HttpTools.loadPicture(it) },
                                contentDescription = "Discord avatar",
                                modifier = Modifier
                                    .size(IMAGE_SIZE, IMAGE_SIZE)
                                    .clip(CircleShape)
                                    .shadow(20.dp, shape = CircleShape)
                            )
                        }

                        Text(saba.self.tag, fontWeight = FontWeight.Bold)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(3, 3, 3))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row {
                        Text("Now Playing: ", fontWeight = FontWeight.Bold, color = Color.White)
                        Text(nowPlaying.value, color = Color.White)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Volume: ", fontWeight = FontWeight.Bold, color = Color.White)
                        Slider(
                            value = volume.value,
                            onValueChange = { volume.value = it; saba.updateVolume() },
                            steps = 10,
                            modifier = Modifier.size(
                                width = min(windowState.size.width - 20.dp, 250.dp),
                                height = 5.dp
                            ),
                        )

                        Text(" ${(volume.value * 100f).roundToInt()}%", color = Color.White)
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(windowState.size)
                    .fillMaxSize()
                    .background(Color(3, 3, 3))
                    .padding(10.dp),
            ) {
                Column(verticalArrangement = Arrangement.Center) {
                    Text("uh check your browser, so you can authenticate with discord lol.", color = Color.White)
                }
            }

            openOauthPage()
        }
    }
}

fun openOauthPage() {
    Desktop.getDesktop().browse(URI(SabaApp.INSTANCE.discord.redirectUri))
}
