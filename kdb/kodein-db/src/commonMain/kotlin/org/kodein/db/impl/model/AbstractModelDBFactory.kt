package org.kodein.db.impl.model

import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.db.data.DataDBFactory
import org.kodein.db.invoke
import org.kodein.db.model.*

abstract class AbstractModelDBFactory : ModelDBFactory {

    protected abstract val ddbFactory: DataDBFactory

    protected abstract fun defaultSerializer(): Serializer<Any>

    protected abstract fun defaultMetadataExtractor(): MetadataExtractor

    final override fun open(path: String, vararg options: Options.Open): ModelDB {
        val serializerOpt: DBSerializer? = options()
        val metadataExtractorOpt: DBMetadataExtractor? = options()
        val typeTableOpt: DBTypeTable? = options()

        val serializer = serializerOpt?.serializer ?: defaultSerializer()
        val metadataExtractor = metadataExtractorOpt?.extractor ?: defaultMetadataExtractor()
        val typeTable = typeTableOpt?.typeTable ?: TypeTable()

        return ModelDBImpl(serializer, metadataExtractor, typeTable, ddbFactory.open(path, *options))
    }

    override fun destroy(path: String, vararg options: Options.Open) {
        ddbFactory.destroy(path, *options)
    }

}
