package org.kodein.db.impl.model.jvm

import org.kodein.db.TypeTable
import org.kodein.db.model.PolymorphicCollection
import kotlin.reflect.KClass

class AnnotationTypeTable : TypeTable {
    private val rootCache = HashMap<KClass<*>, KClass<*>>()
    private val nameCache = HashMap<String, KClass<*>>()

    override fun getTypeName(type: KClass<*>): String = type.java.name

    override fun getTypeClass(name: String): KClass<*>? {
        nameCache[name]?.let { return it }
        return try {
            Class.forName(name).kotlin.also { nameCache[name] = it }
        } catch (_: Throwable) {
            null
        }
    }

    override fun getRegisteredClasses(): Set<KClass<*>> = emptySet()

    override fun getRootOf(type: KClass<*>): KClass<*>? {
        rootCache[type]?.let { return it }

        type.java.getAnnotation(PolymorphicCollection::class.java)?.let {
            rootCache[type] = it.root
            return it.root
        }

        return null
    }

}