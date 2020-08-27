package org.kodein.db

import org.kodein.memory.io.Writeable

public interface Body {

    public fun writeInto(dst: Writeable)

    public companion object {

        public inline operator fun invoke(crossinline block: (Writeable) -> Unit): Body = object : Body {
            override fun writeInto(dst: Writeable) = block(dst)
        }

    }

}
