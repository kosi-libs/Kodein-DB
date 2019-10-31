package org.kodein.db.impl.model

import org.kodein.db.Value
import org.kodein.db.model.orm.Metadata

sealed class Primitive(val id: Value): Metadata {

    override val primaryKey: Value get() = id
}

class IntPrimitive(id: Value, val value: Int) : Primitive(id)
class LongPrimitive(id: Value, val value: Long) : Primitive(id)
class DoublePrimitive(id: Value, val value: Double) : Primitive(id)
