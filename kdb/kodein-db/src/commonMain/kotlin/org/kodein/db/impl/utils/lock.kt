package org.kodein.db.impl.utils

import org.kodein.memory.util.DeferScope
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public expect interface Lock {
    public fun lock()
    public fun unlock()
}
internal expect fun newLock(): Lock

@OptIn(ExperimentalContracts::class)
public inline fun <T> Lock.withLock(action: () -> T): T {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    lock()
    try {
        return action()
    } finally {
        unlock()
    }
}
public fun DeferScope.lockInScope(lock: Lock) {
    lock.lock()
    defer { lock.unlock() }
}

public expect interface RWLock {
    public fun readLock(): Lock
    public fun writeLock(): Lock
}
internal expect fun newRWLock(): RWLock

