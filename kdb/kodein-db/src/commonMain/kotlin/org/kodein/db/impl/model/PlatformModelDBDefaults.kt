package org.kodein.db.impl.model

import org.kodein.db.TypeTable
import org.kodein.db.model.orm.DefaultSerializer
import org.kodein.db.model.orm.MetadataExtractor


internal expect object PlatformModelDBDefaults {

    internal fun serializer(): DefaultSerializer?

    internal fun metadataExtractor(): MetadataExtractor?

    internal fun typeTable(): TypeTable?

}
