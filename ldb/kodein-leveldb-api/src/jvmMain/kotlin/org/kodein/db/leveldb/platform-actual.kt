package org.kodein.db.leveldb

import java.util.*

public actual class StackTrace(private val elements: Array<StackTraceElement>) {

    public actual fun write(on: Appendable) {
        on.append(elements.joinToString("\n"))
    }

    public actual companion object {
        public actual fun current(): StackTrace = StackTrace(Thread.currentThread().stackTrace)
    }

}

public actual fun <T> newWeakHashSet(): MutableSet<T> = Collections.newSetFromMap(WeakHashMap<T, Boolean>())
