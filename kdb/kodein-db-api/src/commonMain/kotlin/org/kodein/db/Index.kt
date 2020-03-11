package org.kodein.db

data class Index(val name: String, val value: Any)

fun indexSet(vararg indexAndValues: Pair<String, Any>): Set<Index> =
        indexAndValues.map { Index(it.first, it.second) } .toHashSet()
