package org.kodein.db.impl.model

import kotlinx.serialization.Serializable
import org.kodein.db.Value
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.model.ModelDB
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.file.FileSystem
import org.kodein.memory.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class ModelDBTests_11_ValueConverters : ModelDBTests() {

    class LDB : ModelDBTests_11_ValueConverters(), ModelDBTests.LDB
    class IM : ModelDBTests_11_ValueConverters(), ModelDBTests.IM


    @Test
    fun test00_uuid_value() {
        val uuid = UUID(42, 21)
        assertEquals(Value.Companion.of(42L, 21L), mdb.valueOf(uuid))
    }


}
