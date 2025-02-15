package app.ams

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import app.ams.dataClasses.Config

object GCLogger {
    private val logger: Logger = LoggerFactory.getLogger("GC-API")
    private var isDebugEnabled: Boolean = false

    fun init(config: Config) {
        isDebugEnabled = config.debugMessages
    }

    fun debug(message: String) {
        println(isDebugEnabled)
        if (isDebugEnabled) {
            logger.debug(message)
        }
    }

    fun info(message: String) {
        logger.info(message)
    }

    fun error(message: String){
        if (isDebugEnabled) {
            logger.error(message)
        }
    }
}
