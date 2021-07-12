package org.kodein.db.model.index

import kotlin.properties.ReadOnlyProperty

public interface IndexDefinition {
    public val pair: Pair<String, Any>
}

public class IndexSingleDefinition<T : Any>(name: String, value: T) : IndexDefinition {
    override val pair: Pair<String, T> = name to value
}

public class IndexPairDefinition<A : Any, B : Any>(name: String, first: A, second: B) : IndexDefinition {
    override val pair: Pair<String, Array<Any>> = name to arrayOf(first, second)
}

public class IndexTripleDefinition<A : Any, B : Any, C : Any>(name: String, first: A, second: B, third: C) : IndexDefinition {
    override val pair: Pair<String, Array<Any>> = name to arrayOf(first, second, third)
}

public class IndexCompositeDefinition(name: String, values: Array<out Any>): IndexDefinition {
    override val pair: Pair<String, Array<out Any>> = name to values
}

/**
 * Creates a new single index. (Index over one property)
 *
 * The name of the index is the name of its property.
 *
 * Usage:
 * ```kt
 * val nameIndex by index(name)
 * ```
 */
public fun <T : Any> index(value: T): ReadOnlyProperty<Any, IndexSingleDefinition<T>> =
    ReadOnlyProperty { _, property -> IndexSingleDefinition(property.name, value) }

/**
 * Creates a new pair index. (Index over two related properties)
 *
 * The name of the index is the name of its property.
 *
 * Usage:
 * ```kt
 * val nameIndex by index(firstName, lastName)
 * ```
 */
public fun <A : Any, B : Any> index(first: A, second: B): ReadOnlyProperty<Any, IndexPairDefinition<A, B>> =
    ReadOnlyProperty { _, property -> IndexPairDefinition(property.name, first, second) }

/**
 * Creates a new pair index. (Index over two related properties)
 *
 * The name of the index is the name of its property.
 *
 * Usage:
 * ```kt
 * val nameIndex by index(firstName, lastName)
 * ```
 */
public fun <A : Any, B : Any, C : Any> index(first: A, second: B, third: C): ReadOnlyProperty<Any, IndexTripleDefinition<A, B, C>> =
    ReadOnlyProperty { _, property -> IndexTripleDefinition(property.name, first, second, third) }

/**
 * Creates a new composite index.
 *
 * The name of the index is the name of its property.
 *
 * Usage:
 * ```kt
 * val nameIndex by index(firstName, middleName, thirdName, lastName)
 * ```
 */
public fun <T : Any> index(vararg values: T): ReadOnlyProperty<Any, IndexCompositeDefinition> =
    ReadOnlyProperty { _, property -> IndexCompositeDefinition(property.name, values) }

/**
 * Creates a map which can be used as a result to the overridden
 * `indexes()` function of [org.kodein.db.model.orm.Metadata].
 *
 * Usage:
 * ```kt
 * override fun indexes() = indexMapOf(nameIndex, ageIndex)
 * ```
 */
public fun indexMapOf(vararg indexes: IndexDefinition): Map<String, Any> = indexes.associate { it.pair }
