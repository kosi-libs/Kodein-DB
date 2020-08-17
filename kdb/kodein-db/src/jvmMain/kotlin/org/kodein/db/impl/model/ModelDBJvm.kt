package org.kodein.db.impl.model

import org.kodein.db.DBFactory
import org.kodein.db.data.DataDB
import org.kodein.db.impl.data.DataDBJvm
import org.kodein.db.model.ModelDB

public object ModelDBJvm : AbstractModelDBJvm() {

    override val ddbFactory: DBFactory<DataDB> get() = DataDBJvm

}

public actual val ModelDB.Companion.default: DBFactory<ModelDB> get() = ModelDBJvm
