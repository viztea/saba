package lol.saba.app

import com.github.natanbc.nativeloader.NativeLibLoader
import com.github.natanbc.nativeloader.SystemNativeLibraryProperties
import com.github.natanbc.nativeloader.system.SystemType
import javafx.application.Application
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.effect.DropShadow
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.paint.ImagePattern
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import lavaplayer.format.StandardAudioDataFormats.DISCORD_PCM_S16_BE
import lavaplayer.manager.DefaultAudioPlayerManager
import lavaplayer.source.SourceRegistry
import lol.saba.app.networking.Actor
import lol.saba.app.networking.ActorSession
import lol.saba.app.util.ConfigUtil
import lol.saba.app.util.Discord
import lol.saba.app.util.Discord.DiscordUser.Companion.IMAGE_SIZE
import lol.saba.app.util.Natives
import lol.saba.common.TrackUtil
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

    val FONT_BOLD = Font.font("SF Pro Display", FontWeight.BLACK, -1.0)

    var nowPlayingText = Text(10.0, 20.0, "nothing").apply {
        font = Font.font("SF Pro Display", FontWeight.NORMAL, -1.0)
        fill = Color.WHITE
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + Job()

    private val discord = Discord(this)

    init {
        players.configuration.outputFormat = DISCORD_PCM_S16_BE
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
        val padding = Insets(10.0, 10.0, 10.0, 10.0)

        /* main vbox */
        val vbox = VBox()
        vbox.padding = padding
        vbox.background = Background(BackgroundFill(Color.web("#030303"), null, null))
        vbox.children.add(Text(10.0, 10.0, "Now Playing:").apply {
            font = Font.font("SF Pro Display", FontWeight.EXTRA_BOLD, -1.0)
            fill = Color.WHITE
        })
        vbox.children.add(nowPlayingText)

        /* header vbox */
        val header = HBox(10.0)
        header.padding = padding

        self.avatarImage?.let { image ->
            val rectangle = Rectangle(0.0, 0.0, IMAGE_SIZE, IMAGE_SIZE)
            rectangle.arcWidth = 100.0 // Corner radius
            rectangle.arcHeight = 100.0

            val pattern = ImagePattern(image)
            rectangle.fill = pattern
            rectangle.effect = DropShadow(20.0, Color.BLACK)

            header.children.add(rectangle)
        }

        header.alignment = Pos.CENTER_LEFT

        Text("${self.username}#${self.discriminator}").apply {
            font = FONT_BOLD
            header.children.add(this)
        }

        header.background = Background(BackgroundFill(Color.web("#7affaf"), null, null))

        /* assign the boxes to the root */
        root.center = vbox
        root.top = header

        /* show the scene */
        val scene = Scene(root, 300.0, 250.0)
        stage.title = "Saba App"
        stage.scene = scene
        stage.show()
    }
}
