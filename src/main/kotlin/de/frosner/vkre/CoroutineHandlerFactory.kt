package de.frosner.vkre

import io.vertx.core.Future
import io.vertx.core.Handler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class CoroutineHandlerFactory(override val coroutineContext: CoroutineContext) : CoroutineScope {

    fun <F, R> create(
        handler: suspend () -> Pair<F, R>,
        failure: suspend (F) -> String? = { null }
    ): Handler<Future<R>> =
        Handler {
            launch(coroutineContext) {
                try {
                    val res = handler()
                    val potentialFailure = failure(res.first)
                    if (potentialFailure != null) {
                        it.fail(potentialFailure)
                    } else {
                        it.complete(res.second)
                    }
                } catch (e: Exception) {
                    it.fail(e)
                }
            }
        }

}