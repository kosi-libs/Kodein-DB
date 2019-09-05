package org.kodein.db.impl

import org.kodein.db.DB
import org.kodein.db.DBFactory
import org.kodein.db.impl.model.ModelDBAndroid
import org.kodein.db.model.ModelDB

object DBAndroid : AbstractDBFactory() {
    override fun mdbFactory(): DBFactory<ModelDB> = ModelDBAndroid
}

actual val DB.Companion.default: DBFactory<DB> get() = DBAndroid
