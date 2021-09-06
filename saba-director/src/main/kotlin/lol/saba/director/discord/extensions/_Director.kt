package lol.saba.director.discord.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import lol.saba.common.messages.SabaMessage
import lol.saba.director.discord.networking.Director
import org.slf4j.LoggerFactory

@PublishedApi
internal val directorOnLogger = LoggerFactory.getLogger("SabaClient.on")

inline fun <reified T : SabaMessage> Director.on(scope: CoroutineScope = this, noinline block: suspend (T) -> Unit): Job {
    return events
        .filterIsInstance<T>()
        .onEach { event ->
            launch {
                event
                    .runCatching { block(this) }
                    .onFailure { directorOnLogger.error("Error occurred while handling ${T::class.qualifiedName}", it) }
            }
        }
        .launchIn(scope)
}
