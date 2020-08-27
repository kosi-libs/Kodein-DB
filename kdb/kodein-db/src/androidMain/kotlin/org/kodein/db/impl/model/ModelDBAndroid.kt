package org.kodein.db.impl.model

import org.kodein.db.DBFactory
import org.kodein.db.data.DataDB
import org.kodein.db.impl.data.DataDBAndroid
import org.kodein.db.model.ModelDB

public object ModelDBAndroid : AbstractModelDBJvm() {

    override val ddbFactory: DBFactory<DataDB> get() = DataDBAndroid

}

public actual val ModelDB.Companion.default: DBFactory<ModelDB> get() = ModelDBAndroid
