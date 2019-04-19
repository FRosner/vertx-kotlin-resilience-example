package de.frosner.vkre

import io.vertx.core.Vertx
import kotlinx.coroutines.runBlocking

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            val vertx = Vertx.vertx()

            try {
                val server = Server(vertx)
                val serverPort = server.start()

                val client = Client(vertx)
                client.sendRequest(
                    port = serverPort,
                    maxFailures = 1,
                    timeout = 2000,
                    fallbackOnFailure = false,
                    resetTimeout = 10000
                )
            } finally {
                vertx.close()
            }
        }
    }
}