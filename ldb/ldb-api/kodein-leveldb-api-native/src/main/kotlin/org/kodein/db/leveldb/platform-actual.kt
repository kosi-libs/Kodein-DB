package org.kodein.db.leveldb

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.SOURCE)
actual annotation class Synchronized()

actual class StackTrace() {

    actual fun write(on: Appendable) {
        // TODO
    }

    actual companion object {
        actual fun current() = StackTrace()
    }

}

actual fun <T> WeakHashSet(): MutableSet<T> = HashSet<T>()
