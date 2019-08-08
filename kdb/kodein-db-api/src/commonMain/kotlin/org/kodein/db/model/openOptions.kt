package org.kodein.db.model

import org.kodein.db.Options
import org.kodein.db.TypeTable

class DBSerializer(val serializer: Serializer<Any>) : Options.Open
class DBMetadataExtractor(val extractor: MetadataExtractor) : Options.Open
class DBTypeTable(val typeTable: TypeTable) : Options.Open
