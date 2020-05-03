package org.kodein.db.test.utils

import androidx.test.platform.app.InstrumentationRegistry
import org.kodein.memory.file.FileSystem

private val androidContext get() = InstrumentationRegistry.getInstrumentation().targetContext

actual fun initPlatform() { FileSystem.registerContext(androidContext) }
