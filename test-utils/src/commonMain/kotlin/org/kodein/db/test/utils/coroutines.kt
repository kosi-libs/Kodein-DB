package org.kodein.db.test.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


expect fun <T> runBlocking(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> T): T

@OptIn(ExperimentalTime::class)
fun runBlockingTest(timeout: Duration = Duration.seconds(10), test: suspend CoroutineScope.() -> Unit) {
    runBlocking {
        withTimeout(timeout) {
            launch {
                test()
                cancel()
            }.join()
        }
    }
}
