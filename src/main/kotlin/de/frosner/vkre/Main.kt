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
                    { ctx -> ctx.response().setStatusCode(200).end() }
                )
                val server = Server(vertx, responses)
                val serverPort = server.start()

                val client = Client(vertx)
                client.sendRequest(
                    port = serverPort,
                    circuitBreakerOptions = circuitBreakerOptionsOf(
                        fallbackOnFailure = false,
                        maxFailures = 5,
                        maxRetries = 2,
                        resetTimeout = 10000,
                        timeout = 2000
                    )
                )
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