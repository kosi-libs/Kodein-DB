package org.kodein.db.inmemory

import org.kodein.db.DBFactory
import org.kodein.db.data.DataDB
import org.kodein.db.impl.model.AbstractModelDBFactory
import org.kodein.db.model.ModelDB


public object ModelDBInMemory : AbstractModelDBFactory() {
    override val ddbFactory: DBFactory<DataDB> get() = DataDBInMemory
}

public val ModelDB.Companion.inMemory: DBFactory<ModelDB> get() = ModelDBInMemory
