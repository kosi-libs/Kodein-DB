package org.kodein.db.leveldb.android

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.FixMethodOrder
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.test.LevelDBTests
import org.kodein.log.Logger
import org.kodein.log.print.printLogFilter


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class LevelDBAndroidTests : LevelDBTests(LevelDBAndroid.internal(InstrumentationRegistry.getContext())) {

    override fun basicOptions(): LevelDB.Options =
            super.basicOptions().copy(loggerFactory = { Logger(it, printLogFilter) })

}
