package lol.saba.app.networking

import kotlinx.coroutines.launch
import lavaplayer.format.AudioDataFormat
import lavaplayer.format.AudioPlayerInputStream
import lavaplayer.format.StandardAudioDataFormats.COMMON_PCM_S16_BE
import lavaplayer.track.AudioTrack
import lol.saba.app.SabaApp
import lol.saba.common.entity.Session
import lol.saba.common.entity.Track
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine


class ActorSession(val saba: SabaApp, val guild: Long, val info: Session) {
    val player = saba.players.createPlayer()
    val format: AudioDataFormat = saba.players.configuration.outputFormat

    var stream: AudioInputStream? = null
    var dataLine: SourceDataLine? = null

    fun shutdown() {
        player.stopTrack()
        stream?.close()
        dataLine?.close()
    }

    fun play(track: Track) {
        val audioTrack = saba.trackUtil.decodeTrack(track.encoded)
        audioTrack.position = track.position
        player.isPaused = track.paused
        play(audioTrack)
    }

    fun play(track: AudioTrack) = saba.actor.launch {
        stream?.close()
        dataLine?.close()

        stream = AudioPlayerInputStream.createStream(player, format, 10000, true)
        dataLine = AudioSystem.getLine(DataLine.Info(SourceDataLine::class.java, stream?.format)) as SourceDataLine

        player.playTrack(track)

        dataLine!!.open(stream!!.format)
        dataLine!!.start()

        val buffer = ByteArray(COMMON_PCM_S16_BE.maximumChunkSize)
        var chunkSize: Int

        while (stream!!.read(buffer).also { chunkSize = it } >= 0) {
            dataLine!!.write(buffer, 0, chunkSize)
        }
    }
}
