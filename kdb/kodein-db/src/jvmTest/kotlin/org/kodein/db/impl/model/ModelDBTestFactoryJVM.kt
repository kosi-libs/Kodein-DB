package org.kodein.db.impl.model

import org.kodein.db.model.ModelDB
import org.kodein.db.model.based
import org.kodein.db.model.jvm.ModelDBJVM

private val platformFactory = ModelDBJVM.based("/tmp/")

actual object ModelDBTestFactory {
    actual fun open(options: ModelDB.OpenOptions): ModelDB = platformFactory.open("modeldb", options)
    actual fun destroy() = platformFactory.destroy("modeldb")
}
