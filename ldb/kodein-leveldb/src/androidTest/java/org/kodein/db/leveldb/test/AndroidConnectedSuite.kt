package org.kodein.db.leveldb.test

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.kodein.memory.file.FileSystem


@RunWith(Suite::class)
@Suite.SuiteClasses(
    LDBTests_00_SimpleOp.LDB::class,
    LDBTests_00_SimpleOp.IM::class,
    LDBTests_01_Snapshot.LDB::class,
    LDBTests_01_Snapshot.IM::class,
    LDBTests_02_Batch.LDB::class,
    LDBTests_02_Batch.IM::class,
    LDBTests_03_Cursor.LDB::class,
    LDBTests_03_Cursor.IM::class,
    LDBTests_04_ReOpen.LDB::class,
    LDBTests_04_ReOpen.IM::class,
    LDBTests_05_ForgetClose.LDB::class,
    LDBTests_05_ForgetClose.IM::class,
    LDBTests_06_OpenPolicy.LDB::class,
    LDBTests_06_OpenPolicy.IM::class,
)
public class AndroidConnectedSuite {

    public companion object {
        @BeforeClass
        @JvmStatic
        public fun setUp() {
            FileSystem.registerContext(InstrumentationRegistry.getInstrumentation().targetContext)
        }
    }

}
