package org.kodein.db


public class IndexData private constructor(public val values: List<Pair<Any, Any?>>) {
    public companion object {
        public fun withAO(value: Any, associatedObject: Any?): IndexData = IndexData(listOf(value to associatedObject))
        public fun multiple(values: Iterable<Any>): IndexData = IndexData(values.map { it to null })
        public fun multipleWithAO(pairs: Iterable<Pair<Any, Any?>>): IndexData = IndexData(pairs.toList())
    }
}
