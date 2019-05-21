package org.kodein.db

import org.kodein.memory.Writeable

interface Body {

    fun writeInto(dst: Writeable)

    companion object {

        inline operator fun invoke(crossinline block: (Writeable) -> Unit) = object : Body {
            override fun writeInto(dst: Writeable) = block(dst)
        }

    }

}
