package org.kodein.db.leveldb

import java.util.*

actual typealias Synchronized = kotlin.jvm.Synchronized

actual class StackTrace(val elements: Array<StackTraceElement>) {

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

actual fun <T> WeakHashSet() = Collections.newSetFromMap(WeakHashMap<T, Boolean>())
