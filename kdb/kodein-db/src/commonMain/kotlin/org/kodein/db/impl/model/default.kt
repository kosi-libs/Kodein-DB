package org.kodein.db.impl.model

import org.kodein.db.DBFactory
import org.kodein.db.model.ModelDB

public expect val ModelDB.Companion.default: DBFactory<ModelDB>
