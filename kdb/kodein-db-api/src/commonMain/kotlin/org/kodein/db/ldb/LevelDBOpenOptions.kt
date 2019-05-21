package org.kodein.db.ldb

import org.kodein.db.Options
import org.kodein.db.leveldb.LevelDB

class LevelDBOpenOptions(val options: LevelDB.Options) : Options.Open

operator fun LevelDB.Options.unaryPlus() = LevelDBOpenOptions(this)
