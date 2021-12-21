package lol.saba.app.util

import com.github.natanbc.nativeloader.NativeLibLoader
import com.github.natanbc.nativeloader.SystemNativeLibraryProperties
import com.github.natanbc.nativeloader.system.SystemType
import lavaplayer.natives.ConnectorNativeLibLoader
import lol.saba.app.SabaApp
import mu.KotlinLogging
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object NativeTools {
    private val log = KotlinLogging.logger {  }
    private val CONNECTOR_LOADER: NativeLibLoader = NativeLibLoader.create(NativeTools::class.java, "connector")
    private const val LOAD_RESULT_NAME = "lavaplayer.common.natives.NativeLibraryLoader\$LoadResult"

    private var LOAD_RESULT: Any? = try {
        val ctor = Class.forName(LOAD_RESULT_NAME)
            .getDeclaredConstructor(Boolean::class.javaPrimitiveType, RuntimeException::class.java)

        ctor.isAccessible = true
        ctor.newInstance(true, null)
    } catch (ex: ReflectiveOperationException) {
        log.error("Unable to create successful load result");
        null
    }

    fun load() {
        try {
            val type = SystemType.detect(SystemNativeLibraryProperties(null, "nativeloader."))
            log.info { "Detected System: type = ${type.osType()}, arch = ${type.architectureType()}" }
            log.info { "Processor Information: ${NativeLibLoader.loadSystemInfo()}" }
        } catch (e: Exception) {
            log.warn(e) {
                "Unable to load system info" + if (e is UnsatisfiedLinkError || e is RuntimeException && e.cause is UnsatisfiedLinkError)
                    ", this isn't an error" else "."
            }
        }

        /* load natives */
        loadConnector()
    }

    private fun loadConnector() {
        try {
            CONNECTOR_LOADER.load()

            val loadersField = ConnectorNativeLibLoader::class.java.getDeclaredField("loaders")
            loadersField.isAccessible = true

            for (i in 0 until 2) {
                // wtf natan
                markLoaded(java.lang.reflect.Array.get(loadersField.get(null), i))
            }

            log.info("\"connector\" natives were loaded successfully.")
        } catch (ex: Exception) {
            log.error("\"connector\" natives failed to load.", ex)
        }
    }

    private fun markLoaded(loader: Any) {
        val previousResultField = loader.javaClass.getDeclaredField("previousResult")
        previousResultField.isAccessible = true
        previousResultField[loader] = LOAD_RESULT
    }
}
