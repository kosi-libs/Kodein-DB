package org.kodein.db.impl

import org.kodein.db.DB
import org.kodein.db.DBFactory
import org.kodein.db.impl.model.ModelDBJvm
import org.kodein.db.model.ModelDB

public object DBJvm : AbstractDBFactory() {
    override fun mdbFactory(): DBFactory<ModelDB> = ModelDBJvm
}

@Suppress("unused")
public actual val DB.Companion.factory: DBFactory<DB> get() = DBJvm
