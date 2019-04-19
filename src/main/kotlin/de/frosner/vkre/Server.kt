package de.frosner.vkre

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.http.listenAwait

class Server(private val vertx: Vertx, private val responses: List<(RoutingContext) -> Unit> = listOf()) {

    private var responseIndex = 0

    private val server by lazy {
        val router = Router.router(vertx)
        router.route().handler {
            if (responses.isEmpty()) {
                println("Received request:  OK")
                it.response().end("OK")
            } else {
                responses[responseIndex++](it)
            }
        }

        vertx.createHttpServer().requestHandler(router)
    }

    suspend fun start(): Int = server.listenAwait().actualPort()

}