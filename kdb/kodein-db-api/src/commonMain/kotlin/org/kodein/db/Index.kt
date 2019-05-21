package org.kodein.db

data class Index(val name: String, val value: Value)

fun indexSet(vararg nameValues: Pair<String, Value>): Set<Index> =
        nameValues.map { Index(it.first, it.second) } .toHashSet()
