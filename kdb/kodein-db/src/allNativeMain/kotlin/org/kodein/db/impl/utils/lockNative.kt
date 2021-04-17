package org.kodein.db.impl.utils


public interface PhonyLock {
    public companion object : PhonyLock {
        public override fun lock() {}
        public override fun unlock() {}
        public override fun writeLock(): Lock = PhonyLock
        public override fun readLock(): Lock = PhonyLock
    }

    public fun lock()
    public fun unlock()
    public fun writeLock(): Lock
    public fun readLock(): Lock
}

public actual typealias Lock = PhonyLock
internal actual fun newLock(): Lock = PhonyLock

public actual typealias RWLock = PhonyLock
internal actual fun newRWLock(): Lock = PhonyLock
