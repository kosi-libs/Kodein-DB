package org.kodein.db.impl.model

import org.kodein.db.TypeTable
import org.kodein.db.impl.model.jvm.AnnotationMetadataExtractor
import org.kodein.db.impl.model.jvm.AnnotationTypeTable
import org.kodein.db.model.orm.MetadataExtractor
import org.kodein.db.model.orm.DefaultSerializer

internal actual object PlatformModelDBDefaults {

    private fun getClass(name: String): Class<*>? =
            try { Class.forName(name) }
            catch (_: ClassNotFoundException) { null }

    internal actual fun serializer(): DefaultSerializer? {
        val serializerClass =
            getClass("org.kodein.db.orm.kryo.KryoSerializer")
                ?:  getClass("org.kodein.db.orm.kotlinx.KotlinxSerializer")

        @Suppress("UNCHECKED_CAST")
        return serializerClass?.getConstructor()?.newInstance() as? DefaultSerializer
    }

    internal actual fun metadataExtractor(): MetadataExtractor? = AnnotationMetadataExtractor()

    internal actual fun typeTable(): TypeTable? = AnnotationTypeTable()
}
