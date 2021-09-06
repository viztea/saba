package lol.saba.app.util

import com.github.natanbc.nativeloader.NativeLibLoader
import lavaplayer.natives.ConnectorNativeLibLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Natives {
    private val logger: Logger = LoggerFactory.getLogger(Natives::class.java)
    private val CONNECTOR_LOADER: NativeLibLoader = NativeLibLoader.create(Natives::class.java, "connector")
    private const val LOAD_RESULT_NAME = "lavaplayer.common.natives.NativeLibraryLoader\$LoadResult"

    private var LOAD_RESULT: Any? = try {
        val ctor = Class.forName(LOAD_RESULT_NAME)
            .getDeclaredConstructor(Boolean::class.javaPrimitiveType, RuntimeException::class.java)

        ctor.isAccessible = true
        ctor.newInstance(true, null)
    } catch (ex: ReflectiveOperationException) {
        logger.error("Unable to create successful load result");
        null;
    }

    fun load() {
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

            logger.info("Loaded \"connector\" natives.")
        } catch (ex: Exception) {
            logger.error("Failed to load \"connector\" natives.", ex)
        }
    }

    private fun markLoaded(loader: Any) {
        val previousResultField = loader.javaClass.getDeclaredField("previousResult")
        previousResultField.isAccessible = true
        previousResultField[loader] = LOAD_RESULT
    }
}
