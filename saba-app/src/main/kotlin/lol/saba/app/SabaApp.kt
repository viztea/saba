package lol.saba.app

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import lavaplayer.format.StandardAudioDataFormats
import lavaplayer.manager.DefaultAudioPlayerManager
import lavaplayer.source.SourceRegistry
import lol.saba.app.networking.Actor
import lol.saba.app.networking.ActorSession
import lol.saba.app.util.ConfigUtil
import lol.saba.app.util.Discord
import lol.saba.app.util.NativeTools
import lol.saba.common.TrackUtil
import mu.KotlinLogging
import java.util.prefs.Preferences
import kotlin.coroutines.CoroutineContext

class SabaApp(val nowPlaying: MutableState<String>, val volume: State<Float>, val loading: MutableState<Boolean>) : CoroutineScope {
    companion object {
        lateinit var INSTANCE: SabaApp

        private val log = KotlinLogging.logger {  }
    }

    val preferences = Preferences.userRoot().node("saba")
    var session: ActorSession? = null
    val discord = Discord(this)

    internal val config = ConfigUtil.load().getConfig("saba")
    internal var players = DefaultAudioPlayerManager()
    internal val trackUtil = TrackUtil(players)
    internal val actor = Actor(this)

    lateinit var self: Discord.DiscordUser

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + Job()

    fun updateVolume() {
        session?.let { it.player.volume = (volume.value * 100).toInt() }
    }

    init {
        INSTANCE = this

        /*  */
        NativeTools.load()

        players.configuration.outputFormat = StandardAudioDataFormats.COMMON_PCM_S16_BE
        SourceRegistry.registerRemoteSources(players)
    }

    suspend fun onAuthenticated() {
        /* attempt to get the current user */
        self = discord.getSelf()
            ?: return

        log.info { "Logged in as ${self.tag}" }

        /* connect to the server. */
        actor.connect()

        /* we're done "loading" */
        loading.value = false
    }

    suspend fun start() {
        if (discord.accessToken != null) {
            onAuthenticated()

            /* we're no longer loading lol */
            loading.value = false
        }
    }
}
