package org.kodein.db

fun TypeTable.Companion.withFullName(builder: TypeTable.Builder.() -> Unit = {}) = TypeTable(
        { it.java.name },
        { try { Class.forName(it).kotlin } catch (_: Throwable) { null } },
        builder
)
