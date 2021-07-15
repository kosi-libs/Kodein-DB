package org.kodein.db.index

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

public abstract class ModelIndex<M> {
    public abstract val model: M

    @PublishedApi
    internal val indexes: ArrayList<IndexDefinition> = ArrayList()

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
    public inline fun <T : Any> index(crossinline valueProvider: M.() -> T): PropertyDelegateProvider<Any, IndexSingleDefinition<T>> =
        PropertyDelegateProvider { _, property ->
            IndexSingleDefinition(property.name, valueProvider.invoke(model))
                .apply { indexes += this }
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
    public inline fun <A : Any, B : Any> indexPair(crossinline valueProvider: M.() -> Pair<A, B>): PropertyDelegateProvider<Any, IndexPairDefinition<A, B>> =
        PropertyDelegateProvider { _, property ->
            val pair = valueProvider.invoke(model)
            IndexPairDefinition(property.name, pair.first, pair.second)
                .apply { indexes += this }
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
    public inline fun <A : Any, B : Any, C : Any> indexTriple(crossinline valueProvider: M.() -> Triple<A, B, C>): PropertyDelegateProvider<Any, IndexTripleDefinition<A, B, C>> =
        PropertyDelegateProvider { _, property ->
            val triple = valueProvider.invoke(model)
            IndexTripleDefinition(property.name, triple.first, triple.second, triple.third)
                .apply { indexes += this }
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
    public inline fun <T : Any> indexComposite(crossinline valueProvider: M.() -> Array<T>): PropertyDelegateProvider<Any, IndexCompositeDefinition> =
        PropertyDelegateProvider { _, property ->
            IndexCompositeDefinition(property.name, valueProvider.invoke(model))
                .apply { indexes += this }
        }
}

public interface IndexDefinition : ReadOnlyProperty<Any, IndexDefinition> {
    public val pair: Pair<String, Any>

    override fun getValue(thisRef: Any, property: KProperty<*>): IndexDefinition = this
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
