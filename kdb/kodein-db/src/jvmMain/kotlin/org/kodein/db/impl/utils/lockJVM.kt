package org.kodein.db.impl.utils

import kotlin.concurrent.write as ktWrite
import kotlin.concurrent.read as ktRead
import kotlin.concurrent.withLock as ktWithLock


actual typealias Lock = java.util.concurrent.locks.ReentrantLock

actual fun newLock(): Lock = Lock()

actual inline fun <T> Lock.withLock(action: () -> T): T = ktWithLock(action)



actual typealias RWLock = java.util.concurrent.locks.ReentrantReadWriteLock

actual fun newRWLock(): RWLock = RWLock()

actual inline fun <T> RWLock.write(action: () -> T): T = ktWrite(action)

actual inline fun <T> RWLock.read(action: () -> T): T = ktRead(action)
