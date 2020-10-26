package org.kodein.db.impl

import org.kodein.db.DB
import org.kodein.db.DBFactory
import org.kodein.db.impl.model.ModelDBNative
import org.kodein.db.model.ModelDB


public object DBNative : AbstractDBFactory() {

    override val mdbFactory: DBFactory<ModelDB> get() = ModelDBNative

}

@Suppress("unused")
public actual val DB.Companion.default: DBFactory<DB> get() = DBNative
