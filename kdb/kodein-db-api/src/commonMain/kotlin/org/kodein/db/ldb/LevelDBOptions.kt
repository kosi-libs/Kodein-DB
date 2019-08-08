package org.kodein.db.ldb

import org.kodein.db.Options
import org.kodein.db.leveldb.LevelDB

class LevelDBOptions(val options: LevelDB.Options) : Options.Open
