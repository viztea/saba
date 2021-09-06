package lol.saba.app

import com.github.natanbc.nativeloader.NativeLibLoader
import com.github.natanbc.nativeloader.SystemNativeLibraryProperties
import com.github.natanbc.nativeloader.system.SystemType
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.stage.Stage
import kotlinx.coroutines.*
import lavaplayer.format.StandardAudioDataFormats.COMMON_PCM_S16_BE
import lavaplayer.manager.DefaultAudioPlayerManager
import lavaplayer.source.SourceRegistry
import lol.saba.app.networking.Actor
import lol.saba.app.networking.ActorSession
import lol.saba.app.util.ConfigUtil
import lol.saba.app.util.Discord
import lol.saba.app.util.Natives
import lol.saba.common.TrackUtil
import lol.saba.common.entity.Session
import org.slf4j.LoggerFactory
import java.util.prefs.Preferences
import kotlin.coroutines.CoroutineContext

class SabaApp : Application(), CoroutineScope {
    companion object {
        private val logger = LoggerFactory.getLogger(SabaApp::class.java)

        @JvmStatic
        fun main(args: Array<out String>) = launch(SabaApp::class.java)
    }

    var session: ActorSession? = null

    internal val config = ConfigUtil.load().getConfig("saba")
    internal val preferences = Preferences.userRoot().node("saba")
    internal var players = DefaultAudioPlayerManager()
    internal val trackUtil = TrackUtil(players)
    internal val actor = Actor(this)

    lateinit var self: Discord.DiscordUser

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + Job()

    private val discord = Discord(this)

    init {
        players.configuration.outputFormat = COMMON_PCM_S16_BE
        SourceRegistry.registerRemoteSources(players)
    }

    override fun start(primaryStage: Stage): Unit = runBlocking {
        /* native libraries */
        try {
            val type = SystemType.detect(SystemNativeLibraryProperties(null, "nativeloader."))
            logger.info("Detected System: type = ${type.osType()}, arch = ${type.architectureType()}")
            logger.info("Processor Information: ${NativeLibLoader.loadSystemInfo()}")
            Natives.load()
        } catch (ex: Exception) {
            val message =
                "Unable to load system info" + if (ex is UnsatisfiedLinkError || ex is RuntimeException && ex.cause is UnsatisfiedLinkError)
                    ", this isn't an error" else "."

            logger.warn(message, ex)
        }

        self = discord.getSelf()
        logger.info("Logged in as ${self.username}#${self.discriminator}")
        actor.connect()
        doUi(primaryStage)
    }

    fun doUi(stage: Stage) {
        val root = BorderPane()

        /* main vbox */
        val vbox = VBox()
        root.center = vbox
        vbox.background = Background(BackgroundFill(Color.web("#030303"), null, null))

        /* header vbox */
        val header = HBox()
        header.padding = Insets(1.0, 1.5, 1.0, 1.5)
        val text = Text("${self.username}#${self.discriminator}")
        header.children.add(text)

        root.top = header
        header.background = Background(BackgroundFill(Color.web("#7affaf"), null, null))

        val scene = Scene(root, 300.0, 250.0)

        stage.title = "Saba App"
        stage.scene = scene
        stage.show()
    }
}
