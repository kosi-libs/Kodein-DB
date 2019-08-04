package org.kodein.db.model.jvm

import org.kodein.db.data.DataDBFactory
import org.kodein.db.data.jvm.DataDBJVM
import org.kodein.db.impl.model.AbstractModelDBFactory
import org.kodein.db.model.MetadataExtractor
import org.kodein.db.model.NoMetadataExtractor
import org.kodein.db.model.Serializer

object ModelDBJVM : AbstractModelDBFactory() {

    override val ddbFactory: DataDBFactory get() = DataDBJVM

    private fun getClass(name: String): Class<*>? =
            try { Class.forName(name) }
            catch (_: ClassNotFoundException) { null }

    override fun defaultSerializer(): Serializer<Any> {
        val serializerClass =
                    getClass("org.kodein.db.orm.kryo.KryoSerializer")
                ?:  getClass("org.kodein.db.orm.kotlinx.KotlinxSerializer")
                ?:  throw IllegalStateException("Could not find neither Kryo nor KotlinX serializers in the classpath. Either add one of them to the classpath or define the serializer with ModelDB.OpenOptions.")

        @Suppress("UNCHECKED_CAST")
        return serializerClass.getConstructor().newInstance() as Serializer<Any>
    }

    override fun defaultMetadataExtractor(): MetadataExtractor {
        // TODO: try to find annotation metadata extractor in the classpath
        return NoMetadataExtractor()
    }

}
