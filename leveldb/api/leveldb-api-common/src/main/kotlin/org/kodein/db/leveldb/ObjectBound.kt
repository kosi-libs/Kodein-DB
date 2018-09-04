package org.kodein.db.leveldb

internal abstract class ObjectBound<T>(private var base: T?, name: String, handler: Handler, options: LevelDB.Options) : PlatformCloseable(name, handler, options) {

    override val isClosed: Boolean get() = base == null

    fun nonNullBase(): T {
        checkIsOpen()
        return base!!
    }

    internal abstract fun release(base: T)

    override fun platformClose() {
        release(base!!)
        base = null
    }
}
