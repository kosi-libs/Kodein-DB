package org.kodein.db.impl.utils


public class PhonyLock private constructor() {
    public companion object {
        internal val instance = PhonyLock()
    }
}

public actual typealias Lock = PhonyLock

internal actual fun newLock(): Lock = PhonyLock.instance

internal actual inline fun <T> Lock.withLock(action: () -> T): T = action()

public actual typealias RWLock = PhonyLock

internal actual fun newRWLock(): Lock = PhonyLock.instance

internal actual inline fun <T> RWLock.write(action: () -> T): T = action()

internal actual inline fun <T> RWLock.read(action: () -> T): T = action()
