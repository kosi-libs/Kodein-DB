package org.kodein.db.leveldb

abstract class NativeBound(private var ptr: Long, name: String, handler: Handler?, options: LevelDB.Options) : PlatformCloseable(name, handler, options) {

    override val isClosed: Boolean get() = ptr == 0L

    fun nonZeroPtr(): Long {
        checkIsOpen()
        return ptr
    }

    protected abstract fun release(ptr: Long)

    override fun platformClose() {
        release(ptr)
        ptr = 0
    }
}
