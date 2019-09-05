package org.kodein.db.impl

import org.kodein.db.DB
import org.kodein.db.DBFactory

expect val DB.Companion.default: DBFactory<DB>
