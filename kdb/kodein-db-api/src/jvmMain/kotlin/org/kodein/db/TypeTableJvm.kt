package org.kodein.db

import org.kodein.memory.io.Memory
import org.kodein.memory.text.Charset
import org.kodein.memory.text.array
import org.kodein.memory.text.readString

public fun TypeTable.Companion.withFullName(builder: TypeTable.Builder.() -> Unit = {}): TypeTable = TypeTable(
        { Memory.array(it.java.name, Charset.UTF8) },
        { try { Class.forName(it.readString(charset = Charset.UTF8)).kotlin } catch (_: Throwable) { null } },
        builder
)
