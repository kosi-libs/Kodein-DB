package org.kodein.db

fun TypeTable.Companion.withFullName(builder: TypeTable.Builder.() -> Unit = {}) = TypeTable({ it.java.name }, builder)
