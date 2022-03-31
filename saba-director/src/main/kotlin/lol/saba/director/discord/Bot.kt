package lol.saba.director.discord

import com.typesafe.config.ConfigFactory
import dev.kord.common.Color
import dev.kord.core.Kord
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.runBlocking
import lavaplayer.manager.DefaultAudioPlayerManager
import lavaplayer.source.SourceRegistry
import lavaplayer.track.AudioTrack
import lavaplayer.track.AudioTrackCollection
import lavaplayer.track.loader.ItemLoadResultAdapter
import lol.saba.common.TrackUtil
import lol.saba.common.messages.c2s.CreateSession
import lol.saba.common.messages.c2s.JoinSession
import lol.saba.director.discord.networking.Director
import lol.saba.director.discord.networking.SabaSession
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

object Bot {
    private val logger = LoggerFactory.getLogger(Bot::class.java)
    private val LINK_REGEX = "^https?://.+".toRegex()

    val sessions = ConcurrentHashMap<Long, SabaSession>()
    val config = ConfigFactory.load()
    val players = DefaultAudioPlayerManager()
    val director = Director()
    val trackUtil = TrackUtil(players)

    @JvmStatic
    fun main(args: Array<out String>) = runBlocking { start() }

    @OptIn(PrivilegedIntent::class)
    suspend fun start() {
        director.connect()

        SourceRegistry.registerRemoteSources(players)

        val kord = Kord(config.getString("token")) {
            intents = Intents.all
        }

        kord.on<ReadyEvent> {
            logger.info("Connected as ${self.tag}!")
        }

        kord.on<MessageCreateEvent> {
//            println(message)
            if (message.content.startsWith("saba ", true)/* && message.author!!.id.value == 396096412116320258L*/) {
                val content = message.content.drop(5)
                val command = content.split(' ').first()
                when (command) {
                    "play" -> play(message, message.getChannel() as GuildChannel)
                    "join" -> joinSession(message, message.getChannel() as GuildChannel)
                    "create" -> createSession(message, message.getChannel() as GuildChannel)
                }
            }
        }

        Runtime.getRuntime().addShutdownHook(thread(false, false) {
            runBlocking {
                director.close("shutting down")
            }
        })

        kord.login()
    }

    private suspend fun joinSession(interaction: Message, channel: GuildChannel): Any {
        val session = sessions[channel.guildId.value]
            ?: return interaction.reply {
                embed {
                    description = "This server doesn't have a session!"
                    color = Color(0xff7a8a)
                }
            }

        director.send(JoinSession(interaction.author!!.id.value, session.info.id))
        return interaction.reply {
            embed {
                description = "Joined the session for this guild."
                color = Color(0x7affaf)

                footer { text = "Use the /join-session command to join it!" }
            }
        }
    }

    private suspend fun createSession(interaction: Message, channel: GuildChannel) {
        director.send(CreateSession(channel.guildId.value))
        interaction.reply {
            embed {
                description = "Created a session for this guild."
                color = Color(0x7affaf)

                footer { text = "Use the /join-session command to join it!" }
            }
        }
    }

    private suspend fun play(interaction: Message, channel: GuildChannel) {
        var query = interaction.content.drop(9)
        if (!LINK_REGEX.matches(query)) {
            val platform = /*interaction.command.options["query"]?.string()
                ?: */"ytsearch:"

            query = "$platform$query"
        }

        val itemLoader = players.items.createItemLoader(query)
        itemLoader.resultHandler = object : ItemLoadResultAdapter() {
            override fun onTrackLoad(track: AudioTrack): Unit = runBlocking {
                sessions[channel.guildId.value]?.play(track)
                    ?: return@runBlocking

                interaction.reply {
                    embed {
                        description = "Now playing [**${track.info.title}**](${track.info.uri})"
                        color = Color(0x7affaf)
                    }
                }
            }

            override fun onCollectionLoad(collection: AudioTrackCollection) {
                onTrackLoad(collection.tracks.first())
            }
        }

        itemLoader.load()
    }
}
