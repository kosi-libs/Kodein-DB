package org.kodein.db.impl.model

import org.kodein.db.*
import org.kodein.db.data.DataDB
import org.kodein.db.model.*
import org.kodein.db.model.orm.MetadataExtractor
import org.kodein.db.model.orm.DefaultSerializer

public abstract class AbstractModelDBFactory : DBFactory<ModelDB> {

    protected abstract val ddbFactory: DBFactory<DataDB>

    final override fun open(path: String, vararg options: Options.Open): ModelDB {
        val serializer = options<DefaultSerializer>() ?: PlatformModelDBDefaults.serializer()
        val metadataExtractors = options.all<MetadataExtractor>() + listOfNotNull(PlatformModelDBDefaults.metadataExtractor())
        val valueConverters = ValueConverter.defaults + options.all<ValueConverter>()
        val typeTable = options<TypeTable>() ?: PlatformModelDBDefaults.typeTable() ?: TypeTable()
        val serializers = options.all<DBClassSerializer<*>>()

        val modelMiddlewares = options.all<Middleware.Model>()

        return modelMiddlewares.fold(
            ModelDBImpl(
                defaultSerializer = serializer,
                userClassSerializers = serializers.associate { it.cls to it.serializer },
                metadataExtractors = metadataExtractors,
                valueConverters = valueConverters,
                typeTable = typeTable,
                data = ddbFactory.open(path, *options)
            ) as ModelDB
        ) { mdb, middleware -> middleware.wrap(mdb) }
    }

    override fun destroy(path: String, vararg options: Options.Open) {
        ddbFactory.destroy(path, *options)
    }

}
