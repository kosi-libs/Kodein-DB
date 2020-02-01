package org.kodein.db

import org.kodein.db.ascii.getAscii
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.wrap
import org.kodein.memory.text.toAsciiBytes

fun TypeTable.Companion.withFullName(builder: TypeTable.Builder.() -> Unit = {}) = TypeTable(
        { KBuffer.wrap(it.java.name.toAsciiBytes()) },
        { try { Class.forName(it.getAscii()).kotlin } catch (_: Throwable) { null } },
        builder
)
