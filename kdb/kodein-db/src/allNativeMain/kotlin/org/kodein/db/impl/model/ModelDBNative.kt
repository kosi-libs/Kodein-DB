package org.kodein.db.impl.model

import org.kodein.db.DBFactory
import org.kodein.db.TypeTable
import org.kodein.db.data.DataDB
import org.kodein.db.impl.data.DataDBNative
import org.kodein.db.model.ModelDB
import org.kodein.db.model.orm.MetadataExtractor
import org.kodein.db.model.orm.DefaultSerializer

public object ModelDBNative : AbstractModelDBFactory() {

    override val ddbFactory: DBFactory<DataDB> get() = DataDBNative

    override fun defaultSerializer(): DefaultSerializer? = null

    override fun defaultMetadataExtractor(): MetadataExtractor? = null

    override fun defaultTypeTable(): TypeTable? = null
}

public actual val ModelDB.Companion.default: DBFactory<ModelDB> get() = ModelDBNative
