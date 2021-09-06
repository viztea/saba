package lol.saba.app.util

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path

object ConfigUtil {
    private val logger = LoggerFactory.getLogger(ConfigUtil::class.java)
    private val path = Path(System.getProperty("user.home"), ".saba", "app.conf")

    fun load(): Config {
        val defaultConfig = ConfigFactory.empty()
            .withFallback(
                ConfigFactory.systemEnvironment()
                    .withFallback(ConfigFactory.systemProperties())
                    .withFallback(ConfigFactory.parseResources("app.conf"))
            )

        val file: File = path.toFile()
        if (!Files.isReadable(path)) {
            file.parentFile.mkdirs()
            if (file.createNewFile()) {
                val default = ConfigUtil::class.java.classLoader.getResourceAsStream("app.conf")
                if (default != null) file.writeBytes(default.readBytes())
            }
        }

        logger.info("Loading config from {}", path.toAbsolutePath())
        return ConfigFactory.parseFile(file)
            .withFallback(defaultConfig)
            .resolve()
    }
}
