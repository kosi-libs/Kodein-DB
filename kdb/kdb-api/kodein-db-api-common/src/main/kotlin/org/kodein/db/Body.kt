package org.kodein.db

import kotlinx.io.core.IoBuffer

interface Body {

    fun writeInto(dst: IoBuffer)

}
