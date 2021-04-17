package org.kodein.db.impl.model.jvm

import org.kodein.db.TypeTable
import org.kodein.db.model.PolymorphicCollection
import org.kodein.memory.io.Memory
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.text.Charset
import org.kodein.memory.text.array
import org.kodein.memory.text.readString
import kotlin.reflect.KClass

public class AnnotationTypeTable : TypeTable {
    private val rootCache = HashMap<KClass<*>, KClass<*>>()
    private val nameCache = HashMap<ReadMemory, KClass<*>>()

    override fun getTypeName(type: KClass<*>): ReadMemory = Memory.array(type.java.name, Charset.UTF8)

    override fun getTypeClass(name: ReadMemory): KClass<*>? {
        nameCache[name]?.let { return it }
        return try {
            Class.forName(name.readString()).kotlin.also { nameCache[name] = it }
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
