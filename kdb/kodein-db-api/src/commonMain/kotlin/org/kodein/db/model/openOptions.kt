package org.kodein.db.model

import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.db.model.orm.MetadataExtractor
import org.kodein.db.model.orm.Serializer
import kotlin.reflect.KClass

class DBSerializer(val serializer: Serializer<Any>) : Options.Open
class DBMetadataExtractor(val extractor: MetadataExtractor) : Options.Open
class DBTypeTable(val typeTable: TypeTable) : Options.Open
class DBClassSerializer<M : Any>(val cls: KClass<M>, val serializer: Serializer<M>) : Options.Open

inline operator fun <reified M : Any> Serializer<M>.unaryPlus() = DBClassSerializer(M::class, this)
