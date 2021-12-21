package lol.saba.app.networking

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lavaplayer.format.AudioDataFormat
import lavaplayer.format.AudioPlayerInputStream
import lavaplayer.manager.event.AudioEventAdapter
import lavaplayer.tools.extensions.addListener
import lavaplayer.track.AudioTrack
import lol.saba.app.SabaApp
import lol.saba.common.entity.Track
import okio.Buffer
import org.slf4j.LoggerFactory
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine

class ActorSession(val saba: SabaApp, val guild: Long) : AudioEventAdapter() {
    companion object {
        private val logger = LoggerFactory.getLogger(ActorSession::class.java)
    }

    internal val player = saba.players
        .createPlayer()
        .also { it.addListener(this) }

    private val format: AudioDataFormat = saba.players.configuration.outputFormat
    private val buffer = Buffer()
    private val stream: AudioInputStream = AudioPlayerInputStream.createStream(player, format, 10000, true)
    private val dataLine: SourceDataLine = AudioSystem.getLine(DataLine.Info(SourceDataLine::class.java, stream.format)) as SourceDataLine

    var polling: Boolean = false

    init {
        saba.updateVolume()
    }

    fun poll() = saba.launch {
        withContext(Dispatchers.IO) {
            logger.debug("Starting to poll data.")

            polling = true
            while (polling) {
                if (player.playingTrack == null) {
                    continue
                }

                /* write the chunk */
                val chunk = stream.readNBytes(format.maximumChunkSize)
                val chunkSize = chunk.size.toLong()

                buffer.write(chunk)

                /* get the size of the written chunk */
                if (chunkSize <= 0) {
                    break
                }

                /* write the buffer value to the data line */
                val byteCount = chunkSize.coerceAtMost(buffer.size)
                val bytes = buffer.readByteArray(byteCount)
                dataLine.write(bytes, 0, byteCount.toInt())
            }

            polling = false
            buffer.clear()
            logger.debug("No longer polling data")
        }
    }

    fun shutdown() {
        player.stopTrack()
        stream.close()
        dataLine.close()
    }

    fun play(track: Track) {
        val audioTrack = saba.trackUtil.decodeTrack(track.encoded)
        audioTrack.position = track.position
        player.isPaused = track.paused
        play(audioTrack)
    }

    fun play(track: AudioTrack) {
        if (!dataLine.isOpen) {
            dataLine.open(stream.format)
        }

        if (!dataLine.isActive) {
            dataLine.start()
        }

        if (!polling) {
            poll()
        }

        saba.nowPlaying.value = track.info.title

        player.startTrack(track, false)
    }
}
