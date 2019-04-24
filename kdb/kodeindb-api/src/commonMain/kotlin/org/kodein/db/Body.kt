package org.kodein.db

import org.kodein.memory.Writeable

interface Body {

    fun writeInto(dst: Writeable)

}
