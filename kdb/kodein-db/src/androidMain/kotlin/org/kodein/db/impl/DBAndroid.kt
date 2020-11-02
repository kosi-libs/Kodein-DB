package org.kodein.db.impl

import org.kodein.db.DB
import org.kodein.db.DBFactory
import org.kodein.db.impl.model.ModelDBAndroid
import org.kodein.db.model.ModelDB

public object DBAndroid : AbstractDBFactory() {
    override val mdbFactory: DBFactory<ModelDB> get() = ModelDBAndroid
}

@Suppress("unused")
public actual val DB.Companion.default: DBFactory<DB> get() = DBAndroid
