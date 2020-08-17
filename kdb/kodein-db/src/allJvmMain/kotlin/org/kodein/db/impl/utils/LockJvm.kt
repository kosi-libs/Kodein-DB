package org.kodein.db.impl.utils

import kotlin.concurrent.read as ktRead
import kotlin.concurrent.withLock as ktWithLock
import kotlin.concurrent.write as ktWrite


public actual typealias Lock = java.util.concurrent.locks.ReentrantLock

internal actual fun newLock(): Lock = Lock()

internal actual inline fun <T> Lock.withLock(action: () -> T): T = ktWithLock(action)



public actual typealias RWLock = java.util.concurrent.locks.ReentrantReadWriteLock

internal actual fun newRWLock(): RWLock = RWLock()

internal actual inline fun <T> RWLock.write(action: () -> T): T = ktWrite(action)

internal actual inline fun <T> RWLock.read(action: () -> T): T = ktRead(action)
