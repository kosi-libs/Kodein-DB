package org.kodein.db.impl.model

import org.kodein.db.DBFactory
import org.kodein.db.model.ModelDB

expect val ModelDB.Companion.default: DBFactory<ModelDB>
