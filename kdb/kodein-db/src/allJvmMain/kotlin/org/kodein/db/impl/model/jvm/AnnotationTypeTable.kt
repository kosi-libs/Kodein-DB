package org.kodein.db.impl.model.jvm

import org.kodein.db.TypeTable
import org.kodein.db.model.PolymorphicCollection
import kotlin.reflect.KClass

class AnnotationTypeTable : TypeTable {
    private val cache = HashMap<KClass<*>, KClass<*>>()

    override fun getTypeName(type: KClass<*>): String = type.java.name

    override fun getTypeClass(name: String): KClass<*>? = try {
        Class.forName(name).kotlin
    } catch (_: Throwable) {
        null
    }

    override fun getRegisteredClasses(): Set<KClass<*>> = emptySet()

    override fun getRootOf(type: KClass<*>): KClass<*>? {
        cache[type]?.let { return it }

        type.java.getAnnotation(PolymorphicCollection::class.java)?.let {
            cache[type] = it.root
            return it.root
        }

        return null
    }

}