package org.kodein.db.test.utils

import androidx.test.platform.app.InstrumentationRegistry

private val androidContext get() = InstrumentationRegistry.getInstrumentation().targetContext

actual val platformTmpPath: String get() = androidContext.cacheDir.absolutePath
