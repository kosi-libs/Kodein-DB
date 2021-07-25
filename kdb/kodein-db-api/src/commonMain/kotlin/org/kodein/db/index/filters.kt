package org.kodein.db.index

import org.kodein.db.*
import kotlin.reflect.KProperty1

public fun interface Filter<M : Any> {
    /**
     * Returns a [Cursor] with all elements in the given database matching
     * this filter.
     */
    public fun find(db: DBRead): Cursor<M>
}

/**
 * Creates a new filter matching all elements where the
 * content of the index equals the given [value].
 */
public inline infix fun <reified M : Any, T : Any> IndexSingleDefinition<M, T>.eq(value: T): Filter<M> =
    Filter { db -> db.find<M>().byIndex(name, value) }

/**
 * Creates a new filter matching all elements where the content
 * of the index equals the given [pair] of values.
 */
public inline infix fun <reified M : Any, A : Any, B : Any> IndexPairDefinition<M, A, B>.eq(pair: Pair<A, B>): Filter<M> =
    Filter { db -> db.find<M>().byIndex(name, pair.first, pair.second) }

/**
 * Creates a new filter matching all elements where the content
 * of the index equals the given [triple] of values.
 */
public inline infix fun <reified M : Any, A : Any, B : Any, C : Any> IndexTripleDefinition<M, A, B, C>.eq(triple: Triple<A, B, C>): Filter<M> =
    Filter { db -> db.find<M>().byIndex(name, triple.first, triple.second, triple.third) }

/**
 * Creates a new filter matching all elements where the content
 * of the composite index matches the given composite index values.
 *
 * Note: the array does not have to satisfy all values of the composite index,
 * it may be incomplete
 *
 * Attention: creating this filter is not typesafe, please make sure that all
 * the given types are valid (the same as the types of the properties of the
 * composite index)
 */
public inline infix fun <reified M : Any> IndexCompositeDefinition<M>.eq(values: Array<Any>): Filter<M> =
    Filter { db -> db.find<M>().byIndex(name, *values) }
