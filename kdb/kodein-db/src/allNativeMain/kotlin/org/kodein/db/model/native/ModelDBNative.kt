package org.kodein.db.model.native

import org.kodein.db.data.DataDBFactory
import org.kodein.db.data.native.DataDBNative
import org.kodein.db.impl.model.AbstractModelDBFactory
import org.kodein.db.model.MetadataExtractor
import org.kodein.db.model.NoMetadataExtractor
import org.kodein.db.model.Serializer

object ModelDBNative : AbstractModelDBFactory() {

    override val ddbFactory: DataDBFactory get() = DataDBNative

    override fun defaultSerializer(): Serializer {
        throw IllegalStateException("Because there is no reflexivity, you need to manually add the KotlinXSerializer in ModelDB.OpenOptions.")
    }

    override fun defaultMetadataExtractor(): MetadataExtractor = NoMetadataExtractor()

}
