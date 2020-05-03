package org.kodein.db.impl

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.kodein.db.impl.data.*
import org.kodein.db.impl.model.*
import org.kodein.db.impl.model.cache.*
import org.kodein.memory.file.FileSystem


@ExperimentalUnsignedTypes
@RunWith(Suite::class)
@Suite.SuiteClasses(
        DataDBTests_00_Put::class,
        DataDBTests_01_Delete::class,
        DataDBTests_02_Get::class,
        DataDBTests_03_FindAll::class,
        DataDBTests_04_FindByType::class,
        DataDBTests_05_FindByID::class,
        DataDBTests_06_Seek::class,
        DataDBTests_07_FindByIndex::class,
        DataDBTests_08_IndexSeek::class,
        DataDBTests_09_Batch::class,
        DataDBTests_10_CloseOpen::class,
        DataDBTests_11_FindIndexes::class,
        DataDBTests_12_Checks::class,
        DataKeysTests_00_Key::class,
        DataKeysTests_01_KeyInfos::class,
        DataKeysTests_02_IndexKey::class,
        DataKeysTests_03_IndexKeyStart::class,
        DataKeysTests_04_IndexKeyInfos::class,
        DataValuesTests::class,

        ModelDBTests_00_PutGetDelete::class,
        ModelDBTests_01_Types::class,
        ModelDBTests_02_IDs::class,
        ModelDBTests_03_Indexes::class,
        ModelDBTests_04_Refs::class,
        ModelDBTests_05_PolymorphicTypeTable::class,
        ModelDBTests_06_All::class,
        ModelDBTests_07_React::class,
        ModelDBTests_08_Primitives::class,
        ModelDBTests_09_Checks::class,
        ModelDBTests_10_MetadataExtractor::class,

        CacheDBTests_00_PutGetDelete::class,
        CacheDBTests_01_React::class,
        CacheDBTests_02_batch::class,
        CacheDBTests_03_Cursor::class,
        CacheDBTests_04_Options::class,
        ModelCacheTests::class,

        DBTests_00_Find::class,
        DBTests_00_Find_NoCache::class,
        DBTests_01_Batch::class,
        DBTests_01_Batch_NoCache::class,
        DBTests_02_Snapshot::class,
        DBTests_02_Snapshot_NoCache::class,
        DBTests_03_Listeners::class
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
