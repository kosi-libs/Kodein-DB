package org.kodein.db

import org.kodein.db.ascii.getAscii
import org.kodein.memory.io.KBuffer
import org.kodein.memory.text.Charset
import org.kodein.memory.text.wrap

public fun TypeTable.Companion.withFullName(builder: TypeTable.Builder.() -> Unit = {}): TypeTable = TypeTable(
        { KBuffer.wrap(it.java.name, Charset.ASCII) },
        { try { Class.forName(it.getAscii()).kotlin } catch (_: Throwable) { null } },
        builder
)
