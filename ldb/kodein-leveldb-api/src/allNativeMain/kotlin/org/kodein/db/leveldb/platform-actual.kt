package org.kodein.db.leveldb


actual class StackTrace(private val elements: Array<String>) {

    actual fun write(on: Appendable) {
        on.append(elements.joinToString("\n"))
    }

    actual companion object {
        actual fun current() = StackTrace(Exception().getStackTrace())
    }

}

actual fun <T> newWeakHashSet(): MutableSet<T> = HashSet()
