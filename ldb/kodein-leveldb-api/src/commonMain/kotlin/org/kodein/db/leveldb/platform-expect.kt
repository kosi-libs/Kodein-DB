package org.kodein.db.leveldb


public expect class StackTrace {
    public fun write(on: Appendable)
    public companion object {
        public fun current(): StackTrace
    }
}

public expect fun <T> newWeakHashSet(): MutableSet<T>
