package org.kodein.db.impl.kv

import org.kodein.db.DBFactory
import org.kodein.db.kv.KeyValueDB

public expect val KeyValueDB.Companion.default: DBFactory<KeyValueDB>
