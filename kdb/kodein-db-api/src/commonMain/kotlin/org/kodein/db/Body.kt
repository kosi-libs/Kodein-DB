package org.kodein.db

import org.kodein.memory.io.Writeable

public fun interface Body {

    public fun writeInto(dst: Writeable)

}
