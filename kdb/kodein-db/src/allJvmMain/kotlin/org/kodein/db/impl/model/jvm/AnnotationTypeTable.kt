package org.kodein.db.impl.model.jvm

import org.kodein.db.TypeTable
import org.kodein.db.ascii.getAscii
import org.kodein.db.model.PolymorphicCollection
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.wrap
import org.kodein.memory.text.toAsciiBytes
import kotlin.reflect.KClass

public class AnnotationTypeTable : TypeTable {
    private val rootCache = HashMap<KClass<*>, KClass<*>>()
    private val nameCache = HashMap<ReadMemory, KClass<*>>()

    override fun getTypeName(type: KClass<*>): ReadMemory = KBuffer.wrap(type.java.name.toAsciiBytes())

    override fun getTypeClass(name: ReadMemory): KClass<*>? {
        nameCache[name]?.let { return it }
        return try {
            Class.forName(name.getAscii()).kotlin.also { nameCache[name] = it }
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
