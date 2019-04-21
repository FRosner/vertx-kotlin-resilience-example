package de.frosner.vkre

import io.vertx.circuitbreaker.CircuitBreaker
import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.circuitbreaker.executeCommandAwait
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.web.client.sendAwait

class FraudCheckService(
    private val vertx: Vertx,
    private val circuitBreaker: CircuitBreaker,
    private val apiUrl: String
) {

    private val client: WebClient = WebClient.create(vertx)

    suspend fun checkFraud(): Boolean {
        val handlerFactory = CoroutineHandlerFactory(vertx.dispatcher())
        val response = circuitBreaker.executeCommandAwait(
            command = handlerFactory.create(
                handler = {
                    print("Requesting...")
                    val response = client.getAbs(apiUrl).sendAwait()
                    Pair(response, response.bodyAsString().toBoolean())
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
        )
        return response
    }

}