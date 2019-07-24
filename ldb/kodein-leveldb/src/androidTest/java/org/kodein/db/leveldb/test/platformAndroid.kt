package org.kodein.db.leveldb.test

import androidx.test.platform.app.InstrumentationRegistry
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.android.LevelDBAndroid
import org.kodein.db.leveldb.based
import org.kodein.log.Logger
import org.kodein.log.print.printLogFilter

actual fun platformOptions() = baseOptions().copy(loggerFactory = { Logger(it, printLogFilter) })

private val androidContext get() = InstrumentationRegistry.getInstrumentation().targetContext

actual val platformFactory: LevelDBFactory = LevelDBAndroid.based(androidContext.dataDir.absolutePath + "/")
