package org.kodein.db.leveldb.jni

import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.PlatformCloseable

public abstract class NativeBound(private var ptr: Long, name: String, handler: Handler?, options: LevelDB.Options) : PlatformCloseable(name, handler, options) {

    public val nonZeroPtr: Long get() {
        checkIsOpen()
        return ptr
    }

    protected abstract fun release(ptr: Long)

    override fun platformClose() {
        release(ptr)
        ptr = 0
    }
}
