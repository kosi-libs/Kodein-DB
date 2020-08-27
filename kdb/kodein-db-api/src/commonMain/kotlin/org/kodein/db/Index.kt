package org.kodein.db

public data class Index(val name: String, val value: Any)

public fun indexSet(vararg indexAndValues: Pair<String, Any>): Set<Index> =
        indexAndValues.map { Index(it.first, it.second) } .toHashSet()
