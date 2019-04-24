package org.kodein.db.leveldb


expect class StackTrace {
    fun write(on: Appendable)
    companion object {
        fun current(): StackTrace
    }
}

expect fun <T> newWeakHashSet(): MutableSet<T>
