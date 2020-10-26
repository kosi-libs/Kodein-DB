package org.kodein.db.impl.model

import org.kodein.db.TypeTable
import org.kodein.db.model.orm.DefaultSerializer
import org.kodein.db.model.orm.MetadataExtractor


internal actual object PlatformModelDBDefaults {
    internal actual fun serializer(): DefaultSerializer? = null

    internal actual fun metadataExtractor(): MetadataExtractor? = null

    internal actual fun typeTable(): TypeTable? = null
}
