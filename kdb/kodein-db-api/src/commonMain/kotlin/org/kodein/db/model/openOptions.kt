package org.kodein.db.model

import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.db.model.orm.MetadataExtractor
import org.kodein.db.model.orm.Serializer

class DBSerializer(val serializer: Serializer<Any>) : Options.Open
class DBMetadataExtractor(val extractor: MetadataExtractor) : Options.Open
class DBTypeTable(val typeTable: TypeTable) : Options.Open
