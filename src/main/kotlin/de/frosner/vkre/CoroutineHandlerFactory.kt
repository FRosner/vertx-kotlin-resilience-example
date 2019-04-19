package de.frosner.vkre

import io.vertx.core.Future
import io.vertx.core.Handler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class CoroutineHandlerFactory(override val coroutineContext: CoroutineContext) : CoroutineScope {

    fun <T> create(handler: suspend () -> T): Handler<Future<T>> =
        Handler {
            launch(coroutineContext) {
                try {
                    it.complete(handler())
                } catch (e: Exception) {
                    it.fail(e)
                }
            }
        }

}