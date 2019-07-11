package org.kodein.db.data

import org.kodein.db.Body
import org.kodein.db.Index
import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.memory.cache.Sized
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.ReadBuffer

interface DataWrite : DataBase {

    fun put(type: String, primaryKey: Value, body: Body, indexes: Set<Index> = emptySet(), vararg options: Options.Write): Int

    fun putAndGetHeapKey(type: String, primaryKey: Value, body: Body, indexes: Set<Index> = emptySet(), vararg options: Options.Write): Sized<KBuffer>

    fun putAndGetNativeKey(type: String, primaryKey: Value, body: Body, indexes: Set<Index> = emptySet(), vararg options: Options.Write): Sized<Allocation>

    fun delete(key: ReadBuffer, vararg options: Options.Write)

}
