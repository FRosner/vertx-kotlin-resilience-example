package de.frosner.vkre

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.kotlin.core.http.listenAwait

class Server(private val vertx: Vertx) {

    private val server by lazy {
        val router = Router.router(vertx)
        router.route().handler {
            println("Received request:  OK")
            it.response().end("OK")
        }

        vertx.createHttpServer().requestHandler(router)
    }

    suspend fun start(): Int = server.listenAwait().actualPort()

}