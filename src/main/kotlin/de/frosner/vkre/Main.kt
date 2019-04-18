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
                client.sendRequest(serverPort)
            } finally {
                vertx.close()
            }
        }
    }
}