package org.kodein.db

import org.kodein.memory.io.KBuffer
import org.kodein.memory.text.Charset
import org.kodein.memory.text.readString
import org.kodein.memory.text.wrap

public fun TypeTable.Companion.withFullName(builder: TypeTable.Builder.() -> Unit = {}): TypeTable = TypeTable(
        { KBuffer.wrap(it.java.name, Charset.UTF8) },
        { try { Class.forName(it.duplicate().readString()).kotlin } catch (_: Throwable) { null } },
        builder
)
