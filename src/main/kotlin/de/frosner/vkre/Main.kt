package de.frosner.vkre

import io.vertx.circuitbreaker.CircuitBreaker
import io.vertx.core.Vertx
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.circuitbreaker.circuitBreakerOptionsOf
import kotlinx.coroutines.runBlocking

object Main {
    suspend fun <T> tryOrPrint(f: suspend () -> T): Unit = try {
        println("Fraud: ${f()}")
    } catch (t: Throwable) {
        println(t)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            val vertx = Vertx.vertx()

            try {
                val responses: List<(RoutingContext) -> Unit> = listOf(
                    { ctx -> ctx.response().setStatusCode(500).end() },
                    { ctx -> println(" Timing out") },
                    { ctx -> println(" Timing out") },
                    { ctx -> ctx.response().setStatusCode(200).end("true") }
                )
                val server = FraudCheckApi(vertx, responses)
                val serverPort = server.start()


                /*
                 * @param fallbackOnFailure  Sets whether or not the fallback is executed on failure, even when the circuit is closed.
                 * @param maxFailures  Sets the maximum number of failures before opening the circuit.
                 * @param maxRetries  Configures the number of times the circuit breaker tries to redo the operation before failing.
                 * @param resetTimeout  Sets the time in ms before it attempts to re-close the circuit (by going to the half-open state). If the circuit is closed when the timeout is reached, nothing happens. <code>-1</code> disables this feature.
                 * @param timeout  Sets the timeout in milliseconds. If an action is not completed before this timeout, the action is considered as a failure.
                 */
                val options = circuitBreakerOptionsOf(
                    fallbackOnFailure = false,
                    maxFailures = 1,
                    maxRetries = 2,
                    resetTimeout = 5000,
                    timeout = 2000
                )

                val circuitBreaker = CircuitBreaker.create("my-circuit-breaker", vertx, options)
                val client = FraudCheckService(vertx, circuitBreaker, "http://localhost:$serverPort/")

                tryOrPrint { client.checkFraud() }
                tryOrPrint { client.checkFraud() }
                println("Waiting for circuit to be open again...")
                Thread.sleep(6000)
                tryOrPrint { client.checkFraud() }
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