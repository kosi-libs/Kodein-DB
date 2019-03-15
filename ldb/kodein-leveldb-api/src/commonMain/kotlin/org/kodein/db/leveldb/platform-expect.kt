package org.kodein.db.leveldb

expect interface PlatformLevelDB {
    interface WriteBatch
    interface Cursor
}

expect class StackTrace {
    fun write(on: Appendable)
    companion object {
        fun current(): StackTrace
    }
}

expect fun <T> WeakHashSet(): MutableSet<T>
