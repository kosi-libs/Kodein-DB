package org.kodein.db.impl.model

import org.kodein.db.data.DataDB
import org.kodein.db.data.DataDBFactory
import org.kodein.db.data.based
import org.kodein.db.data.native.DataDBNative
import org.kodein.db.model.ModelDB
import org.kodein.db.model.based
import org.kodein.db.model.native.ModelDBNative

private val platformFactory = ModelDBNative.based("/tmp/")

actual object ModelDBTestFactory {
    actual fun open(options: ModelDB.OpenOptions): ModelDB = platformFactory.open("modeldb", options)
    actual fun destroy() = platformFactory.destroy("modeldb")
}
