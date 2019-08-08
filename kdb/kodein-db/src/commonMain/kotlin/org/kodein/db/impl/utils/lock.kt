package org.kodein.db.impl.utils

expect class Lock
internal expect fun newLock(): Lock

internal expect inline fun <T> Lock.withLock(action: () -> T): T


expect class RWLock
internal expect fun newRWLock(): RWLock

internal expect inline fun <T> RWLock.write(action: () -> T): T
internal expect inline fun <T> RWLock.read(action: () -> T): T

