package lol.saba.common

import lavaplayer.tools.io.MessageInput
import lavaplayer.tools.io.MessageOutput
import lavaplayer.track.AudioTrack
import lavaplayer.track.TrackEncoder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

class TrackUtil(val trackEncoder: TrackEncoder) {
    /**
     * Base64 decoder used by [decode]
     */
    private val decoder: Base64.Decoder by lazy {
        Base64.getDecoder()
    }

    /**
     * Base64 encoder used by [encode]
     */
    private val encoder: Base64.Encoder by lazy {
        Base64.getEncoder()
    }

    /**
     * Encodes an audio track into a base64 string.
     * @param track The audio track to encode.
     * @return The encoded base64
     */
    fun encodeTrack(track: AudioTrack): String {
        val outputStream = ByteArrayOutputStream()
        return outputStream.use {
            trackEncoder.encodeTrack(MessageOutput(it), track)
            encoder.encodeToString(it.toByteArray())
        }
    }

    /**
     * Decodes an base64 encoded track to a usable AudioTrack.
     * @param track
     *        The base64 encoded track.
     *
     * @return The usable AudioTrack
     */
    fun decodeTrack(track: String): AudioTrack {
        val inputStream = ByteArrayInputStream(decoder.decode(track))
        return inputStream.use { trackEncoder.decodeTrack(MessageInput(it))!!.decodedTrack!! }
    }
}
