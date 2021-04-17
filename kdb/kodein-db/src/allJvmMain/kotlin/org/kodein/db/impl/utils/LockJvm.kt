package org.kodein.db.impl.utils


public actual typealias Lock = java.util.concurrent.locks.Lock
internal actual fun newLock(): Lock = java.util.concurrent.locks.ReentrantLock()


public actual typealias RWLock = java.util.concurrent.locks.ReadWriteLock
internal actual fun newRWLock(): RWLock = java.util.concurrent.locks.ReentrantReadWriteLock()
