package de.frosner.vkre

import io.vertx.circuitbreaker.CircuitBreaker
import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.circuitbreaker.circuitBreakerOptionsOf
import io.vertx.kotlin.circuitbreaker.executeCommandAwait
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.web.client.sendAwait

class Client(private val vertx: Vertx) {

    private val client: WebClient = WebClient.create(vertx)

    suspend fun sendRequest(
        port: Int,
        maxFailures: Int,
        timeout: Long,
        fallbackOnFailure: Boolean,
        resetTimeout: Long
    ) {
        val breaker = CircuitBreaker.create(
            "my-circuit-breaker", vertx, circuitBreakerOptionsOf(
                maxFailures = maxFailures,
                timeout = timeout,
                fallbackOnFailure = fallbackOnFailure,
                resetTimeout = resetTimeout
            )
        )
        val handlerFactory = CoroutineHandlerFactory(vertx.dispatcher())
        val response = breaker.executeCommandAwait(
            handlerFactory.create {
                client.get(port, "localhost", "/").sendAwait().bodyAsString()
            }
        )
        println("Received response: $response")
    }

}