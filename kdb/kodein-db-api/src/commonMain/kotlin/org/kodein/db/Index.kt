package org.kodein.db

data class Index(val index: String, val value: Value)

fun indexSet(vararg indexAndValues: Pair<String, Value>): Set<Index> =
        indexAndValues.map { Index(it.first, it.second) } .toHashSet()
