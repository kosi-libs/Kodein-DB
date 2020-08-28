package org.kodein.db.impl.model.jvm

import org.kodein.db.Index
import org.kodein.db.Options
import org.kodein.db.model.Id
import org.kodein.db.model.Indexed
import org.kodein.db.model.orm.Metadata
import org.kodein.db.model.orm.MetadataExtractor
import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Method

public class AnnotationMetadataExtractor : MetadataExtractor {

    private sealed class ValueGetter {

        abstract val member: Member

        abstract fun get(model: Any): Any?

        class OfMethod(override val member: Method) : ValueGetter() {
            init {
                if (member.parameterCount != 0)
                    error("@Id or @Indexed annotated methods must have no argument")
            }

            override fun get(model: Any): Any? = member.invoke(model)
        }

        class OfField(override val member: Field) : ValueGetter() {
            override fun get(model: Any): Any? {
                @Suppress("DEPRECATION") val wasAccessible = member.isAccessible
                if (!wasAccessible) member.isAccessible = true
                try {
                    return member.get(model)
                } finally {
                    if (!wasAccessible) member.isAccessible = false
                }
            }
        }
    }

    private class ValueGetters(val id: ValueGetter?, val indexes: Map<String, ValueGetter>) {
        fun merge(from: Class<*>, other: ValueGetters): ValueGetters {
            if (id != null && other.id != null && !(id.member is Method && other.id.member is Method && id.member.name == other.id.member.name)) error("$from has two IDs: ${other.id.member} AND ${id.member}")
            val intersect = indexes.keys.intersect(other.indexes.keys).filterNot {
                val thisMember = indexes[it]?.member
                val otherMember = other.indexes[it]?.member
                thisMember is Method && otherMember is Method && thisMember.name == otherMember.name
            }
            if (intersect.isNotEmpty()) {
                error(buildString {
                    append("$from has indexes that are defined twice.\n")
                    intersect.forEach {
                        append("    $it: ${other.indexes[it]?.member} AND ${this@ValueGetters.indexes[it]?.member}\n")
                    }
                })
            }
            return ValueGetters(id ?: other.id, other.indexes + indexes)
        }
        companion object {
            val EMPTY = ValueGetters(null, emptyMap())
        }
    }

    private val cache = HashMap<Class<*>, ValueGetters>()

    private fun Class<*>.getters(): ValueGetters {
        cache[this]?.let { return it }
        if (this == Any::class) return ValueGetters.EMPTY

        val id = declaredFields.find { it.isAnnotationPresent(Id::class.java) }?.let { ValueGetter.OfField(it) }
                ?: declaredMethods.find { it.isAnnotationPresent(Id::class.java) }?.let { ValueGetter.OfMethod(it) }

        val indexes = declaredMethods.mapNotNull { m -> m.getAnnotation(Indexed::class.java)?.let { it.name to ValueGetter.OfMethod(m) } } +
                declaredFields.mapNotNull { f -> f.getAnnotation(Indexed::class.java)?.let { it.name to ValueGetter.OfField(f) } }

        val duplicates = indexes.groupBy { it.first } .filter { it.value.size >= 2 }
        if (duplicates.isNotEmpty()) {
            error(buildString {
                append("${this@getters} has indexes that are defined twice.\n")
                duplicates
                        .mapValues { e -> e.value.map { it.second } }
                        .forEach { e ->
                            append("    ${e.key}:")
                            e.value.forEachIndexed { index, getter ->
                                if (index != 0) append(" AND ")
                                append(" ${getter.member}")
                            }
                            append("\n")
                        }
            })


        }

        val getters = ValueGetters (id, indexes.toMap())
                .merge(this, interfaces?.takeIf { it.isNotEmpty() } ?.map { it.getters() } ?.reduce { acc, vg -> acc.merge(this, vg) } ?: ValueGetters.EMPTY)
                .merge(this, superclass?.getters() ?: ValueGetters.EMPTY)

        cache[this] = getters

        return getters
    }

    override fun extractMetadata(model: Any, vararg options: Options.Write): Metadata? {
        val type = model.javaClass
        val getters = type.getters()

        getters.id ?: return null
        val id = getters.id.get(model) ?: error("Id cannot be null in $model")
        val indexes = getters.indexes.mapNotNullTo(HashSet()) { entry -> entry.value.get(model)?.let { Index(entry.key, it) } }

        return Metadata(id, indexes)
    }

}
