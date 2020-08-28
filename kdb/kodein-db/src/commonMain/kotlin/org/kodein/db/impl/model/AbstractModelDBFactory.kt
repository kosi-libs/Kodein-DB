package org.kodein.db.impl.model

import org.kodein.db.*
import org.kodein.db.data.DataDB
import org.kodein.db.model.*
import org.kodein.db.model.orm.MetadataExtractor
import org.kodein.db.model.orm.DefaultSerializer

public abstract class AbstractModelDBFactory : DBFactory<ModelDB> {

    protected abstract val ddbFactory: DBFactory<DataDB>

    protected abstract fun defaultSerializer(): DefaultSerializer?

    protected abstract fun defaultMetadataExtractor(): MetadataExtractor?

    protected abstract fun defaultTypeTable(): TypeTable?

    final override fun open(path: String, vararg options: Options.Open): ModelDB {
        val serializer = options<DefaultSerializer>() ?: defaultSerializer()
        val metadataExtractor = options.all<MetadataExtractor>() + listOfNotNull(defaultMetadataExtractor())
        val typeTable = options<TypeTable>() ?: defaultTypeTable() ?: TypeTable()
        val serializers = options.all<DBClassSerializer<*>>()

        val modelMiddlewares = options.all<Middleware.Model>().map { it.middleware }

        return modelMiddlewares.fold(ModelDBImpl(serializer, serializers.associate { it.cls to it.serializer }, metadataExtractor, typeTable, ddbFactory.open(path, *options)) as ModelDB) { mdb, middleware -> middleware(mdb) }
    }

    override fun destroy(path: String, vararg options: Options.Open) {
        ddbFactory.destroy(path, *options)
    }

}
