package de.frosner.vkre

import io.vertx.circuitbreaker.CircuitBreaker
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.circuitbreaker.executeCommandAwait
import io.vertx.kotlin.circuitbreaker.executeCommandWithFallbackAwait
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.web.client.sendAwait
import java.util.function.Function

class FraudCheckService(
    private val vertx: Vertx,
    private val circuitBreaker: CircuitBreaker,
    private val apiUrl: String
) {

    private val client: WebClient = WebClient.create(vertx)
    private val handlerFactory = CoroutineHandlerFactory(vertx.dispatcher())

    suspend fun checkFraud(): Boolean {
        return circuitBreaker.executeCommandAwait(
            command = handlerFactory.create(
                handler = {
                    print("Requesting...")
                    val response = client.getAbs(apiUrl).sendAwait()
                    Pair<HttpResponse<Buffer>, Boolean>(response, response.bodyAsString().toBoolean())
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
    }

    suspend fun checkFraudWithFallback(totalPrice: Int): Boolean {
        val handlerFactory = CoroutineHandlerFactory(vertx.dispatcher())
        val response = circuitBreaker.executeCommandWithFallbackAwait(
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
            ),
            fallback = Function {
                totalPrice > 100
            }
        )
        return response
    }

}