package org.kodein.db.index

import kotlin.properties.ReadOnlyProperty

public abstract class ModelIndex<M>(public val model: M) {
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
    public inline fun <T : Any> index(crossinline valueProvider: M.() -> T): ReadOnlyProperty<Any, IndexSingleDefinition<T>> =
        ReadOnlyProperty { _, property -> IndexSingleDefinition(property.name, valueProvider.invoke(model)) }

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
    public inline fun <A : Any, B : Any> indexPair(crossinline valueProvider: M.() -> Pair<A, B>): ReadOnlyProperty<Any, IndexPairDefinition<A, B>> =
        ReadOnlyProperty { _, property ->
            val pair = valueProvider.invoke(model)
            IndexPairDefinition(property.name, pair.first, pair.second)
        }

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
    public inline fun <A : Any, B : Any, C : Any> indexTriple(crossinline valueProvider: M.() -> Triple<A, B, C>): ReadOnlyProperty<Any, IndexTripleDefinition<A, B, C>> =
        ReadOnlyProperty { _, property ->
            val triple = valueProvider.invoke(model)
            IndexTripleDefinition(property.name, triple.first, triple.second, triple.third)
        }

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
    public inline fun <T : Any> indexComposite(crossinline valueProvider: M.() -> Array<T>): ReadOnlyProperty<Any, IndexCompositeDefinition> =
        ReadOnlyProperty { _, property -> IndexCompositeDefinition(property.name, valueProvider.invoke(model)) }
}

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

public class IndexCompositeDefinition(name: String, values: Array<out Any>) : IndexDefinition {
    override val pair: Pair<String, Array<out Any>> = name to values
}

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
