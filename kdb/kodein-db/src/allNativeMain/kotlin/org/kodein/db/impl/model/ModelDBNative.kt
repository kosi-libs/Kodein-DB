package org.kodein.db.impl.model

import org.kodein.db.DBFactory
import org.kodein.db.data.DataDB
import org.kodein.db.impl.data.DataDBNative
import org.kodein.db.model.*
import org.kodein.db.model.orm.MetadataExtractor
import org.kodein.db.model.orm.NoMetadataExtractor
import org.kodein.db.model.orm.Serializer

object ModelDBNative : AbstractModelDBFactory() {

    override val ddbFactory: DBFactory<DataDB> get() = DataDBNative

    override fun defaultSerializer(): Serializer<Any>? = null

    override fun defaultMetadataExtractor(): MetadataExtractor = NoMetadataExtractor()

}

actual val ModelDB.Companion.default: DBFactory<ModelDB> get() = ModelDBNative
