package org.kodein.db.plugin.fts

import org.kodein.db.impl.model.default
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.model.ModelDB
import org.kodein.db.test.utils.byteArray
import org.kodein.memory.file.FileSystem
import kotlin.test.Test

abstract class FtsDBTests_00_Put : FtsDBTests() {

    class LDB : FtsDBTests_00_Put() { override val factory = ModelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : FtsDBTests_00_Put() { override val factory = ModelDB.inMemory }

    @Test fun test00_simplePut() {
        mdb.put(Contact(1, "Salomon BRYS", "Paris"))

        assertFtsDBIs(
            byteArray("XftsR", 0, "o", 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, "name") to byteArray("BRYS", 0, "Salomon", 0),
            byteArray("XftsT", 0, "BRYS", 0, "name", 0, "o", 0, 0, 0, 0, 1, 0, 0, 0, 1, 0) to byteArray(0, 0, 0, 8),
            byteArray("XftsT", 0, "Salomon", 0, "name", 0, "o", 0, 0, 0, 0, 1, 0, 0, 0, 1, 0) to byteArray(0, 0, 0, 0)
        )
    }

}
