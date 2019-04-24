package org.kodein.db.leveldb

import java.util.*

actual class StackTrace(private val elements: Array<StackTraceElement>) {

    actual fun write(on: Appendable) {
        var i = 0
        while (i < elements.size && !elements[i].className.startsWith("com.soberdb.leveldb"))
            ++i
        while (i < elements.size && elements[i].className.startsWith("com.soberdb.leveldb"))
            ++i
        while (i < elements.size) {
            on.append("\n    at ").append(elements[i].toString())
            ++i
        }
    }

    actual companion object {
        actual fun current() = StackTrace(Thread.currentThread().stackTrace)
    }

}

actual fun <T> newWeakHashSet(): MutableSet<T> = Collections.newSetFromMap(WeakHashMap<T, Boolean>())
