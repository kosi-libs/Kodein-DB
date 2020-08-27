package org.kodein.db.impl.model.jvm

import org.kodein.db.TypeTable
import org.kodein.db.ascii.getAscii
import org.kodein.db.model.PolymorphicCollection
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.wrap
import org.kodein.memory.text.toAsciiBytes
import kotlin.reflect.*

public class AnnotationTypeTable : TypeTable {
    private val rootCache = HashMap<KType, KType>()
    private val nameCache = HashMap<ReadMemory, KType>()

    override fun getTypeName(type: KType): ReadMemory = KBuffer.wrap((type.classifier as KClass<*>).java.name.toAsciiBytes())

    private fun KClass<*>.toKType(): KType = object : KType {
        override val arguments: List<KTypeProjection> = emptyList()
        override val classifier: KClassifier? = this@toKType
        override val isMarkedNullable: Boolean = false
        override val annotations: List<Annotation> = emptyList()
    }

    override fun getType(name: ReadMemory): KType? {
        nameCache[name]?.let { return it }
        return try {
            Class.forName(name.getAscii()).kotlin.toKType().also { nameCache[name] = it }
        } catch (_: Throwable) {
            null
        }
    }

    override fun getRegisteredTypes(): Set<KType> = emptySet()

    override fun getRootOf(type: KType): KType? {
        rootCache[type]?.let { return it }

        (type.classifier as KClass<*>).java.getAnnotation(PolymorphicCollection::class.java)?.let {
            val rootType = it.root.toKType()
            rootCache[type] = rootType
            return rootType
        }

        return null
    }
}
