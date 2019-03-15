package org.kodein.db.leveldb


actual class StackTrace {

    actual fun write(on: Appendable) {
        on.append(Exception().getStackTrace().joinToString("\n"))
    }

    actual companion object {
        actual fun current() = StackTrace()
    }

}

actual fun <T> WeakHashSet(): MutableSet<T> = HashSet()
