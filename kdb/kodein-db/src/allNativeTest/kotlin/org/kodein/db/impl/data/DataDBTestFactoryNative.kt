package org.kodein.db.impl.data

import org.kodein.db.data.DataDB
import org.kodein.db.data.DataDBFactory
import org.kodein.db.data.based
import org.kodein.db.data.native.DataDBNative

private val platformFactory = DataDBNative.based("/tmp/")

actual object DataDBTestFactory {
    actual fun open(): DataDB = platformFactory.open("datadb")
    actual fun destroy() = platformFactory.destroy("datadb")
}
