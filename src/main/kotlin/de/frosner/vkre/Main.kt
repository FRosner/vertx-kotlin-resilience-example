package de.frosner.vkre

import io.vertx.core.Vertx
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.circuitbreaker.circuitBreakerOptionsOf
import kotlinx.coroutines.runBlocking

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            val vertx = Vertx.vertx()

            try {
                val responses: List<(RoutingContext) -> Unit> = listOf(
                    { ctx -> ctx.response().setStatusCode(500).end() },
                    { ctx -> println(" Timing out") },
                    { ctx -> println(" Timing out") },
                    { ctx -> ctx.response().setStatusCode(200).end() }
                )
                val server = Server(vertx, responses)
                val serverPort = server.start()

                val client = Client(
                    vertx, circuitBreakerOptionsOf(
                        fallbackOnFailure = false,
                        maxFailures = 1,
                        maxRetries = 2,
                        resetTimeout = 5000,
                        timeout = 2000
                    )
                )
                try {
                    client.sendRequest(port = serverPort)
                } catch (e: Throwable) {
                    println(e)
                }
                try {
                    client.sendRequest(port = serverPort)
                } catch (e: Throwable) {
                    println(e)
                }
                println("Waiting for circuit to be open again...")
                Thread.sleep(6000)
                try {
                    client.sendRequest(port = serverPort)
                } catch (e: Throwable) {
                    println(e)
                }
            } finally {
                vertx.close()
            }
        }
    }

    // Timeout, no fallback
    // Timeout, fallback
    // Status code not matching, fallback
    // Status code not matching, no fallback
    // Fallback on failure
    // Fallback with open circuit
    // Success after retry
    // Recovery from open circuit
}