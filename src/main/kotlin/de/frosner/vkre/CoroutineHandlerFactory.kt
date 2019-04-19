package de.frosner.vkre

import io.vertx.core.Future
import io.vertx.core.Handler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class CoroutineHandlerFactory(override val coroutineContext: CoroutineContext) : CoroutineScope {

    fun <T> create(handler: suspend () -> T, failure: suspend (T) -> String? = { null }): Handler<Future<T>> =
        Handler {
            launch(coroutineContext) {
                try {
                    val res = handler()
                    val potentialFailure = failure(res)
                    if (potentialFailure != null) {
                        it.fail(potentialFailure)
                    } else {
                        it.complete(res)
                    }
                } catch (e: Exception) {
                    it.fail(e)
                }
            }
        }

}