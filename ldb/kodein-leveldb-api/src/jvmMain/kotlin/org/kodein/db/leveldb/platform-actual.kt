package org.kodein.db.leveldb

import java.util.*

actual class StackTrace(private val elements: Array<StackTraceElement>) {

    actual fun write(on: Appendable) {
        on.append(elements.joinToString("\n"))
    }

    actual companion object {
        actual fun current() = StackTrace(Thread.currentThread().stackTrace)
    }

}

actual fun <T> newWeakHashSet(): MutableSet<T> = Collections.newSetFromMap(WeakHashMap<T, Boolean>())
