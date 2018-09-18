package org.kodein.db.leveldb

expect interface PlatformLevelDB {
    interface WriteBatch
    interface Iterator
}

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.SOURCE)
expect annotation class Synchronized()

expect class StackTrace {
    fun write(on: Appendable)
    companion object {
        fun current(): StackTrace
    }
}

expect fun <T> WeakHashSet(): MutableSet<T>
