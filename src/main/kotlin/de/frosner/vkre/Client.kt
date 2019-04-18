package de.frosner.vkre

import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.ext.web.client.sendAwait

class Client(vertx: Vertx) {

    private val client: WebClient = WebClient.create(vertx)

    suspend fun sendRequest(port: Int) {
        val response = client.get(port, "localhost", "/").sendAwait().bodyAsString()
        println("Received response: $response")
    }

}