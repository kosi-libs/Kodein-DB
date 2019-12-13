package org.kodein.db.impl.model

import org.kodein.db.TypeTable
import org.kodein.db.impl.model.jvm.AnnotationMetadataExtractor
import org.kodein.db.impl.model.jvm.AnnotationTypeTable
import org.kodein.db.model.orm.MetadataExtractor
import org.kodein.db.model.orm.Serializer

abstract class AbstractModelDBJvm : AbstractModelDBFactory() {

    private fun getClass(name: String): Class<*>? =
            try { Class.forName(name) }
            catch (_: ClassNotFoundException) { null }

    final override fun defaultSerializer(): Serializer<Any>? {
        val serializerClass =
                    getClass("org.kodein.db.orm.kryo.KryoSerializer")
                ?:  getClass("org.kodein.db.orm.kotlinx.KotlinxSerializer")

        @Suppress("UNCHECKED_CAST")
        return serializerClass?.getConstructor()?.newInstance() as? Serializer<Any>
    }

    final override fun defaultMetadataExtractor(): MetadataExtractor = AnnotationMetadataExtractor()

    final override fun defaultTypeTable(): TypeTable? = AnnotationTypeTable()
}
