package de.frosner.vkre

import io.vertx.circuitbreaker.CircuitBreaker
import io.vertx.circuitbreaker.CircuitBreakerOptions
import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.circuitbreaker.executeCommandAwait
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.web.client.sendAwait

class Client(private val vertx: Vertx) {

    private val client: WebClient = WebClient.create(vertx)

    /*
     * @param fallbackOnFailure  Sets whether or not the fallback is executed on failure, even when the circuit is closed.
     * @param maxFailures  Sets the maximum number of failures before opening the circuit.
     * @param maxRetries  Configures the number of times the circuit breaker tries to redo the operation before failing.
     * @param resetTimeout  Sets the time in ms before it attempts to re-close the circuit (by going to the half-open state). If the circuit is closed when the timeout is reached, nothing happens. <code>-1</code> disables this feature.
     * @param timeout  Sets the timeout in milliseconds. If an action is not completed before this timeout, the action is considered as a failure.
     */
    suspend fun sendRequest(
        port: Int,
        circuitBreakerOptions: CircuitBreakerOptions
    ) {
        val breaker = CircuitBreaker.create(
            "my-circuit-breaker", vertx, circuitBreakerOptions
        )
        val handlerFactory = CoroutineHandlerFactory(vertx.dispatcher())
        val response = breaker.executeCommandAwait(
            handlerFactory.create(
                handler = {
                    print("Requesting...")
                    client.get(port, "localhost", "/").sendAwait()
                },
                failure = {
                    when (it.statusCode()) {
                        200 -> {
                            println(" Success (200)")
                            null
                        }
                        else -> {
                            println(" Failure (${it.statusCode()})")
                            "Status code was ${it.statusCode()}"
                        }
                    }
                }
            )
        ).statusCode()
        println("Final response: $response")
    }

}