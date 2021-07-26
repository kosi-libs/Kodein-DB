package org.kodein.db.index

import org.kodein.db.*

public fun interface Filter<M : Any> {
    /**
     * Returns a [Cursor] with all elements in the given database matching
     * this filter.
     */
    public fun find(db: DBRead): Cursor<M>
}

/**
 * Creates a new filter matching all elements where the content of the index **equals** the given [value].
 * Results will be ordered by index value.
 */
public inline infix fun <reified M : Any, T : Any> IndexSingleDefinition<M, T>.eq(value: T): Filter<M> =
    Filter { db -> db.find<M>().byIndex(name, value) }

/**
 * Creates a new filter matching all elements where the content of the index **starts with** the given [value].
 * Results will be ordered by index value.
 *
 * Note that the type must support prefix comparison (such as [String] or [ByteArray]).
 */
public inline infix fun <reified M : Any, T : Any> IndexSingleDefinition<M, T>.sw(value: T): Filter<M> =
    Filter { db -> db.find<M>().byIndex(name, value, isOpen = true) }

/**
 * Creates a new filter matching all elements where the content of the index **equals** the given [pair] of values.
 * Results will be ordered by index value.
 */
public inline infix fun <reified M : Any, T1 : Any, T2 : Any> IndexPairDefinition<M, T1, T2>.eq(pair: Pair<T1, T2>): Filter<M> =
    Filter { db -> db.find<M>().byIndex(name, pair.first, pair.second) }

/**
 * Creates a new filter matching all elements where the content of the index first value **equals** the given [pair]
 * first value and the content of the index second value **starts with** the given [pair] second value.
 * Results will be ordered by index value.
 */
public inline infix fun <reified M : Any, T1 : Any, T2 : Any> IndexPairDefinition<M, T1, T2>.sw(pair: Pair<T1, T2>): Filter<M> =
    Filter { db -> db.find<M>().byIndex(name, pair.first, pair.second, isOpen = true) }

/**
 * Creates a new filter matching all elements where the content of the index first value **equals** the given [value].
 * Results will be ordered by index value.
 */
public inline infix fun <reified M : Any, T1 : Any> IndexPairDefinition<M, T1, *>.peq(value: T1): Filter<M> =
    Filter { db -> db.find<M>().byIndex(name, value) }

/**
 * Creates a new filter matching all elements where the content of the index first value **starts with**
 * the given [value].
 * Results will be ordered by index value.
 *
 * Note that the second type must support prefix comparison (such as [String] or [ByteArray]).
 */
public inline infix fun <reified M : Any, T1 : Any> IndexPairDefinition<M, T1, *>.psw(value: T1): Filter<M> =
    Filter { db -> db.find<M>().byIndex(name, value, isOpen = true) }

/**
 * Creates a new filter matching all elements where the content of the index **equals** the given [triple] of values.
 * Results will be ordered by index value.
 */
public inline infix fun <reified M : Any, T1 : Any, T2 : Any, T3 : Any> IndexTripleDefinition<M, T1, T2, T3>.eq(triple: Triple<T1, T2, T3>): Filter<M> =
    Filter { db -> db.find<M>().byIndex(name, triple.first, triple.second, triple.third) }

/**
 * Creates a new filter matching all elements where the content of the index first and second values **equals**
 * the given [triple] first and second values,
 * and the content of the index third value **starts with** the given [triple] third value.
 * Results will be ordered by index value.
 *
 * Note that the third type must support prefix comparison (such as [String] or [ByteArray]).
 */
public inline infix fun <reified M : Any, T1 : Any, T2 : Any, T3 : Any> IndexTripleDefinition<M, T1, T2, T3>.sw(triple: Triple<T1, T2, T3>): Filter<M> =
    Filter { db -> db.find<M>().byIndex(name, triple.first, triple.second, triple.third, isOpen = true) }

/**
 * Creates a new filter matching all elements where the content of the index first and second value **equals**
 * the given [pair].
 * Results will be ordered by index value.
 */
public inline infix fun <reified M : Any, T1 : Any, T2 : Any> IndexTripleDefinition<M, T1, T2, *>.peq(pair: Pair<T1, T2>): Filter<M> =
    Filter { db -> db.find<M>().byIndex(name, pair.first, pair.second) }

/**
 * Creates a new filter matching all elements where the content of the index first value **equals** the given [pair]
 * first value and the content of the index second value **starts with** the given [pair] second value.
 * Results will be ordered by index value.
 *
 * Note that the second type must support prefix comparison (such as [String] or [ByteArray]).
 */
public inline infix fun <reified M : Any, T1 : Any, T2 : Any> IndexTripleDefinition<M, T1, T2, *>.psw(pair: Pair<T1, T2>): Filter<M> =
    Filter { db -> db.find<M>().byIndex(name, pair.first, pair.second, isOpen = true) }

/**
 * Creates a new filter matching all elements where the content of the index first value **equals** the given [value].
 * Results will be ordered by index value.
 */
public inline infix fun <reified M : Any, T1 : Any> IndexTripleDefinition<M, T1, *, *>.peq(value: T1): Filter<M> =
    Filter { db -> db.find<M>().byIndex(name, value) }

/**
 * Creates a new filter matching all elements where the content of the index first value **starts with**
 * the given [value].
 * Results will be ordered by index value.
 *
 * Note that the type must support prefix comparison (such as [String] or [ByteArray]).
 */
public inline infix fun <reified M : Any, T1 : Any> IndexTripleDefinition<M, T1, *, *>.psw(value: T1): Filter<M> =
    Filter { db -> db.find<M>().byIndex(name, value, isOpen = true) }

/**
 * Creates a new filter matching all elements where the content of the composite index **equals** the given [values].
 *
 * Note: the array does not have to satisfy all values of the composite index, it may be incomplete
 *
 * Attention: creating this filter is not typesafe, please make sure that all the given types are valid
 * (the same as the types of the properties of the composite index).
 */
public inline infix fun <reified M : Any> IndexCompositeDefinition<M>.eq(values: Array<Any>): Filter<M> =
    Filter { db -> db.find<M>().byIndex(name, *values) }

/**
 * Creates a new filter matching all elements where the content of the composite index **equals** all but last
 * given [values] and **starts with** the last of the [values].
 *
 * Attention: creating this filter is not typesafe, please make sure that all the given types are valid
 * (the same as the types of the properties of the composite index).
 *
 * Note that the last type must support prefix comparison (such as [String] or [ByteArray]).
 */
public inline infix fun <reified M : Any> IndexCompositeDefinition<M>.sw(values: Array<Any>): Filter<M> =
    Filter { db -> db.find<M>().byIndex(name, *values, isOpen = true) }

/**
 * Creates a new filter matching all elements where the content of the composite index first value **equals**
 * the given [value].
 *
 * Attention: creating this filter is not typesafe, please make sure that the given type is valid (the same as
 * the type of the first property of the composite index).
 */
public inline infix fun <reified M : Any> IndexCompositeDefinition<M>.peq(value: Any): Filter<M> =
    Filter { db -> db.find<M>().byIndex(name, value) }

/**
 * Creates a new filter matching all elements where the content of the composite index first value **starts with**
 * the given [value].
 *
 * Attention: creating this filter is not typesafe, please make sure that the given type is valid (the same as
 * the type of the first property of the composite index).
 *
 * Note that the given type must support prefix comparison (such as [String] or [ByteArray]).
 */
public inline infix fun <reified M : Any> IndexCompositeDefinition<M>.psw(value: Any): Filter<M> =
    Filter { db -> db.find<M>().byIndex(name, value, isOpen = true) }

/**
 * Creates a new filter matching all elements in an index.
 * Results will be ordered by index value.
 */
public inline fun <reified M : Any> byIndex(index: IndexDefinition<M>): Filter<M> =
    Filter { db -> db.find<M>().byIndex(index.name) }

/**
 * Creates a new filter matching all elements in an index.
 * Results will be ordered by index value.
 */
public inline operator fun <reified M : Any> IndexDefinition<M>.unaryPlus(): Filter<M> = byIndex(this)
