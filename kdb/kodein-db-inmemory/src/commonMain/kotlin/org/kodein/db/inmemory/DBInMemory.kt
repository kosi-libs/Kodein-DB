package org.kodein.db.inmemory

import org.kodein.db.DB
import org.kodein.db.DBFactory
import org.kodein.db.impl.AbstractDBFactory
import org.kodein.db.model.ModelDB

public object DBInMemory : AbstractDBFactory() {
    override val mdbFactory: DBFactory<ModelDB> get() = ModelDBInMemory
}

@Suppress("unused")
public val DB.Companion.inMemory: DBFactory<DB> get() = DBInMemory
