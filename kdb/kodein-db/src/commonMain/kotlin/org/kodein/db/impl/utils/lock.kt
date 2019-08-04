package org.kodein.db.impl.utils

expect class Lock
expect fun newLock(): Lock

expect inline fun <T> Lock.withLock(action: () -> T): T


expect class RWLock
expect fun newRWLock(): RWLock

expect inline fun <T> RWLock.write(action: () -> T): T
expect inline fun <T> RWLock.read(action: () -> T): T

