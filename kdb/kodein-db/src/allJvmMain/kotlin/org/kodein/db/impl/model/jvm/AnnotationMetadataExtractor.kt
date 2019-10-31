package org.kodein.db.impl.model.jvm

import org.kodein.db.Index
import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.db.model.*
import org.kodein.db.model.orm.Metadata
import org.kodein.db.model.orm.MetadataExtractor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

class AnnotationMetadataExtractor : MetadataExtractor {

    private sealed class ValueGetter {
        protected abstract fun result(model: Any): Any?
        fun get(model: Any): Value? {
            val res = result(model) ?: return null

            if (res is Collection<*>) {
                @Suppress("UNCHECKED_CAST")
                return Value.ofAll(*(res.toTypedArray().requireNoNulls()))
            } else {
                return Value.ofAll(res)
            }
        }

        class OfMethod(val method: Method) : ValueGetter() {
            init {
                if (method.parameterCount != 0)
                    error("@PrimaryKey annotated methods must have no argument")
            }

            override fun result(model: Any): Any? = method.invoke(model)
        }

        class OfField(val field: Field) : ValueGetter() {
            override fun result(model: Any): Any? = field.get(model)
        }
    }

    private class ValueGetters(val primaryKeyGetter: ValueGetter, val indexGetters: Map<String, ValueGetter>, val indexSetMethods: List<(Any) -> Set<Index>>)

    private val cache = HashMap<Class<*>, ValueGetters>()

    override fun extractMetadata(model: Any, vararg options: Options.Write): Metadata {

        val type = model.javaClass

        val getters = cache.getOrPut(type) {
            val pk = type.methods.find { it.isAnnotationPresent(Id::class.java) }?.let { ValueGetter.OfMethod(it) }
                    ?: type.fields.find { it.isAnnotationPresent(Id::class.java) }?.let { ValueGetter.OfField(it) }
                    ?: error("$type has no primary key")

            val indexGetters = (
                    type.methods.mapNotNull { m -> m.getAnnotation(Indexed::class.java)?.let { it.name to ValueGetter.OfMethod(m) } } +
                    type.fields.mapNotNull { f -> f.getAnnotation(Indexed::class.java)?.let { it.name to ValueGetter.OfField(f) } }
            ).toMap()

            val indexMethods = type.methods.filter { it.isAnnotationPresent(Indexes::class.java) }
                    .onEach {
                        if (it.returnType != Set::class.java || (it.genericReturnType as ParameterizedType).actualTypeArguments[0] != Index::class.java)
                            error("@Indexes methods must return Set<Index>, $it returns ${it.genericReturnType}.")
                    }
                    .map { ({ model: Any ->
                        @Suppress("UNCHECKED_CAST")
                        it.invoke(model) as Set<Index>
                    }) }

            ValueGetters(pk, indexGetters, indexMethods)
        }

        val primaryKey = getters.primaryKeyGetter.get(model) ?: error("Primary key cannot be null in $model")

        val indexes = getters.indexGetters.mapNotNullTo(HashSet()) { entry -> entry.value.get(model)?.let { Index(entry.key, it) } }

        getters.indexSetMethods.map { it.invoke(model) } .forEach { indexes.addAll(it) }

        return Metadata(primaryKey, indexes)
    }

}