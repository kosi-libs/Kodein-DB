package org.kodein.db.index

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

/**
 * Classes which inherit from this abstract class can be used to define indexes
 * for model.
 * These classes should be defined inside the other class.
 */
public abstract class ModelIndexes<M> {

    private val indexes: ArrayList<IndexDefinition<M>> = ArrayList()

    /**when a `Map<String, Any>`
     * Creates a new single index. (Index over one property)
     *
     * The name of the index is the name of its property.
     *
     * Usage:
     * ```kt
     * val name by index { name }
     * ```
     */
    public fun <T : Any> index(
        name: String? = null,
        valueProvider: M.() -> T
    ): PropertyDelegateProvider<ModelIndexes<M>, ReadOnlyProperty<ModelIndexes<M>, IndexSingleDefinition<M, T>>> =
        PropertyDelegateProvider { _, property ->
            val indexDef = IndexSingleDefinition(name ?: property.name, valueProvider)
                .apply { indexes += this }
            ReadOnlyProperty { _, _ -> indexDef }
        }

    /**
     * Creates a new pair index. (Index over two related properties)
     *
     * The name of the index is the name of its property.
     *
     * Usage:
     * ```kt
     * val name by indexPair { firstName to lastName }
     * ```
     */
    public fun <T1 : Any, T2 : Any> indexPair(
        name: String? = null,
        valueProvider: M.() -> Pair<T1, T2>,
    ): PropertyDelegateProvider<ModelIndexes<M>, ReadOnlyProperty<ModelIndexes<M>, IndexPairDefinition<M, T1, T2>>> =
        PropertyDelegateProvider { _, property ->
            val indexDef = IndexPairDefinition(name ?: property.name, valueProvider)
                .apply { indexes += this }
            ReadOnlyProperty { _, _ -> indexDef }
        }

    /**
     * Creates a new triple index. (Index over three related properties)
     *
     * The name of the index is the name of its property.
     *
     * Usage:
     * ```kt
     * val name by indexTriple { Triple(firstName, secondName, lastName) }
     * ```
     */
    public fun <T1 : Any, T2 : Any, T3 : Any> indexTriple(
        name: String? = null,
        valueProvider: M.() -> Triple<T1, T2, T3>,
    ): PropertyDelegateProvider<ModelIndexes<M>, ReadOnlyProperty<ModelIndexes<M>, IndexTripleDefinition<M, T1, T2, T3>>> =
        PropertyDelegateProvider { _, property ->
            val indexDef = IndexTripleDefinition(name ?: property.name, valueProvider)
                .apply { indexes += this }
            ReadOnlyProperty { _, _ -> indexDef }
        }

    /**
     * Creates a new composite index.
     *
     * The name of the index is the name of its property.
     *
     * Usage:
     * ```kt
     * val name by indexComposite { arrayOf(firstName, middleName, thirdName, lastName) }
     * ```
     */
    public fun <T : Any> indexComposite(
        name: String? = null,
        valueProvider: M.() -> Array<T>
    ): PropertyDelegateProvider<ModelIndexes<M>, ReadOnlyProperty<ModelIndexes<M>, IndexCompositeDefinition<M>>> =
        PropertyDelegateProvider { _, property ->
            val indexDef = IndexCompositeDefinition(name ?: property.name, valueProvider)
                .apply { indexes += this }
            ReadOnlyProperty { _, _ -> indexDef }
        }

    public fun of(model: M): Map<String, Any> =
        indexes.associate { it.name to it.valueProvider.invoke(model) }
}

public interface IndexDefinition<M> {
    public val name: String
    public val valueProvider: M.() -> Any
}

public class IndexSingleDefinition<M, T : Any>(
    override val name: String,
    override val valueProvider: M.() -> T
) : IndexDefinition<M>

public class IndexPairDefinition<M, T1 : Any, T2 : Any>(
    override val name: String,
    override val valueProvider: M.() -> Pair<T1, T2>
) : IndexDefinition<M>

public class IndexTripleDefinition<M, T1 : Any, T2 : Any, T3 : Any>(
    override val name: String,
    override val valueProvider: M.() -> Triple<T1, T2, T3>
) : IndexDefinition<M>

public class IndexCompositeDefinition<M>(
    override val name: String,
    override val valueProvider: M.() -> Array<out Any>
) : IndexDefinition<M>
