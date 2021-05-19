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
    DataDBTests_00_Put.LDB::class,
    DataDBTests_00_Put.IM::class,
    DataDBTests_01_Delete.LDB::class,
    DataDBTests_01_Delete.IM::class,
    DataDBTests_02_Get.LDB::class,
    DataDBTests_02_Get.IM::class,
    DataDBTests_03_FindAll.LDB::class,
    DataDBTests_03_FindAll.IM::class,
    DataDBTests_04_FindByType.LDB::class,
    DataDBTests_04_FindByType.IM::class,
    DataDBTests_05_FindByID.LDB::class,
    DataDBTests_05_FindByID.IM::class,
    DataDBTests_06_Seek.LDB::class,
    DataDBTests_06_Seek.IM::class,
    DataDBTests_07_FindByIndex.LDB::class,
    DataDBTests_07_FindByIndex.IM::class,
    DataDBTests_08_IndexSeek.LDB::class,
    DataDBTests_08_IndexSeek.IM::class,
    DataDBTests_09_Batch.LDB::class,
    DataDBTests_09_Batch.IM::class,
    DataDBTests_10_CloseOpen.LDB::class,
    DataDBTests_10_CloseOpen.IM::class,
    DataDBTests_11_FindIndexes.LDB::class,
    DataDBTests_11_FindIndexes.IM::class,
    DataDBTests_12_Checks.LDB::class,
    DataDBTests_12_Checks.IM::class,

    DataKeysTests_00_DocumentKey::class,
    DataKeysTests_01_IndexKey::class,
    DataKeysTests_02_RefKey::class,

    DataValuesTests::class,

    ModelDBTests_00_PutGetDelete.LDB::class,
    ModelDBTests_00_PutGetDelete.IM::class,
    ModelDBTests_00_PutGetDelete.Encrypted.LDB::class,
    ModelDBTests_00_PutGetDelete.Encrypted.IM::class,
    ModelDBTests_01_Types.LDB::class,
    ModelDBTests_01_Types.IM::class,
    ModelDBTests_01_Types.Encrypted.LDB::class,
    ModelDBTests_01_Types.Encrypted.IM::class,
    ModelDBTests_02_IDs.LDB::class,
    ModelDBTests_02_IDs.IM::class,
    ModelDBTests_03_Indexes.LDB::class,
    ModelDBTests_03_Indexes.IM::class,
    ModelDBTests_03_Indexes.Encrypted.LDB::class,
    ModelDBTests_03_Indexes.Encrypted.IM::class,
    ModelDBTests_04_Refs.LDB::class,
    ModelDBTests_04_Refs.IM::class,
    ModelDBTests_04_Refs.Encrypted.LDB::class,
    ModelDBTests_04_Refs.Encrypted.IM::class,
    ModelDBTests_05_PolymorphicTypeTable.LDB::class,
    ModelDBTests_05_PolymorphicTypeTable.IM::class,
    ModelDBTests_05_PolymorphicTypeTable.Encrypted.LDB::class,
    ModelDBTests_05_PolymorphicTypeTable.Encrypted.IM::class,
    ModelDBTests_06_All.LDB::class,
    ModelDBTests_06_All.IM::class,
    ModelDBTests_06_All.Encrypted.LDB::class,
    ModelDBTests_06_All.Encrypted.IM::class,
    ModelDBTests_07_React.LDB::class,
    ModelDBTests_07_React.IM::class,
    ModelDBTests_07_React.Encrypted.LDB::class,
    ModelDBTests_07_React.Encrypted.IM::class,
    ModelDBTests_08_Primitives.LDB::class,
    ModelDBTests_08_Primitives.IM::class,
    ModelDBTests_09_Checks.LDB::class,
    ModelDBTests_09_Checks.IM::class,
    ModelDBTests_09_Checks.Encrypted.LDB::class,
    ModelDBTests_09_Checks.Encrypted.IM::class,
    ModelDBTests_10_MetadataExtractor.LDB::class,
    ModelDBTests_10_MetadataExtractor.IM::class,
    ModelDBTests_10_MetadataExtractor.Encrypted.LDB::class,
    ModelDBTests_10_MetadataExtractor.Encrypted.IM::class,
    ModelDBTests_11_ValueConverters.LDB::class,
    ModelDBTests_11_ValueConverters.IM::class,

    CacheDBTests_00_PutGetDelete.LDB::class,
    CacheDBTests_00_PutGetDelete.IM::class,
    CacheDBTests_01_React.LDB::class,
    CacheDBTests_01_React.IM::class,
    CacheDBTests_02_Batch.LDB::class,
    CacheDBTests_02_Batch.IM::class,
    CacheDBTests_03_Cursor.LDB::class,
    CacheDBTests_03_Cursor.IM::class,
    CacheDBTests_04_Options.LDB::class,
    CacheDBTests_04_Options.IM::class,

    ModelCacheTests::class,

    DBTests_00_Find.LDB::class,
    DBTests_00_Find.IM::class,
    DBTests_00_Find.NoCache.LDB::class,
    DBTests_00_Find.NoCache.IM::class,
    DBTests_00_Find.Encrypted.LDB::class,
    DBTests_00_Find.Encrypted.IM::class,
    DBTests_00_Find.Encrypted.NoCache.LDB::class,
    DBTests_00_Find.Encrypted.NoCache.IM::class,
    DBTests_01_Batch.LDB::class,
    DBTests_01_Batch.IM::class,
    DBTests_01_Batch.NoCache.LDB::class,
    DBTests_01_Batch.NoCache.IM::class,
    DBTests_01_Batch.Encrypted.LDB::class,
    DBTests_01_Batch.Encrypted.IM::class,
    DBTests_01_Batch.Encrypted.NoCache.LDB::class,
    DBTests_01_Batch.Encrypted.NoCache.IM::class,
    DBTests_02_Snapshot.LDB::class,
    DBTests_02_Snapshot.IM::class,
    DBTests_02_Snapshot.NoCache.LDB::class,
    DBTests_02_Snapshot.NoCache.IM::class,
    DBTests_02_Snapshot.Encrypted.LDB::class,
    DBTests_02_Snapshot.Encrypted.IM::class,
    DBTests_02_Snapshot.Encrypted.NoCache.LDB::class,
    DBTests_02_Snapshot.Encrypted.NoCache.IM::class,
    DBTests_03_Listeners.LDB::class,
    DBTests_03_Listeners.IM::class,
    DBTests_03_Listeners.Encrypted.LDB::class,
    DBTests_03_Listeners.Encrypted.IM::class,
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
