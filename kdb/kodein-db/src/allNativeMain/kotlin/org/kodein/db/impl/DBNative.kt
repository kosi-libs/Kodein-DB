package org.kodein.db.impl

import org.kodein.db.DB
import org.kodein.db.DBFactory
import org.kodein.db.impl.model.ModelDBNative
import org.kodein.db.model.ModelDB


object DBNative : AbstractDBFactory() {

    override fun mdbFactory(): DBFactory<ModelDB> = ModelDBNative

}

@Suppress("unused")
actual val DB.Companion.default: DBFactory<DB> get() = DBNative
