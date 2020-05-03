package org.kodein.db.leveldb.test

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.kodein.memory.file.FileSystem


@RunWith(Suite::class)
@Suite.SuiteClasses(
        LDBTests_00_SimpleOp::class,
        LDBTests_01_Snapshot::class,
        LDBTests_02_Batch::class,
        LDBTests_03_Cursor::class,
        LDBTests_04_ReOpen::class,
        LDBTests_05_ForgetClose::class,
        LDBTests_06_OpenPolicy::class
)
class AndroidConnectedSuite {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUp() {
            FileSystem.registerContext(InstrumentationRegistry.getInstrumentation().targetContext)
        }
    }

}