package org.kodein.db

import org.kodein.memory.io.Writeable

public fun interface Body {

    public fun writeInto(dst: Writeable)

}

@Suppress("NOTHING_TO_INLINE")
public inline fun Writeable.putBody(value: Body): Unit = value.writeInto(this)
