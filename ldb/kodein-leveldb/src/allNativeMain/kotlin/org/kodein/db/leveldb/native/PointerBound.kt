package org.kodein.db.leveldb.native

import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.PlatformCloseable

public abstract class PointerBound<T : CPointed>(private var ptr: CPointer<T>?, name: String, handler: Handler?, options: LevelDB.Options) : PlatformCloseable(name, handler, options) {

    public val nonNullPtr: CPointer<T> get() {
        checkIsOpen()
        return ptr!!
    }

    protected abstract fun release(ptr: CPointer<T>)

    override fun platformClose() {
        if (ptr != null)
            release(ptr!!)
        ptr = null
    }
}
