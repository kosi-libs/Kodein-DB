package org.kodein.db.impl.model

import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.db.data.DataDBFactory
import org.kodein.db.invoke
import org.kodein.db.model.MetadataExtractor
import org.kodein.db.model.ModelDB
import org.kodein.db.model.ModelDBFactory
import org.kodein.db.model.Serializer

abstract class AbstractModelDBFactory : ModelDBFactory {

    protected abstract val ddbFactory: DataDBFactory

    protected abstract fun defaultSerializer(): Serializer<Any>

    protected abstract fun defaultMetadataExtractor(): MetadataExtractor

    override fun open(path: String, vararg options: Options.Open): ModelDB {
        val opt: ModelDB.OpenOptions? = options()

        val serializer = opt?.serializer ?: defaultSerializer()
        val metadataExtractor = opt?.metadataExtractor ?: defaultMetadataExtractor()
        val typeTable = opt?.typeTable ?: TypeTable()

        return ModelDBImpl(serializer, metadataExtractor, typeTable, ddbFactory.open(path, *options))
    }

    override fun destroy(path: String, vararg options: Options.Open) {
        ddbFactory.destroy(path, *options)
    }

}
