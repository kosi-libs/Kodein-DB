package org.kodein.db.model

import org.kodein.db.TypeTable
import org.kodein.memory.io.ReadMemory

public interface ModelTypeMatcher {

    public val typeTable: TypeTable
    public fun getTypeId(typeName: ReadMemory, createIfNone: Boolean = true): Int
    public fun getTypeName(typeId: Int): ReadMemory?

}
