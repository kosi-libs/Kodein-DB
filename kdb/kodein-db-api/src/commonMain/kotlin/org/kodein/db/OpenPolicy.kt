package org.kodein.db

import org.kodein.db.ldb.LevelDBOptions
import org.kodein.db.leveldb.LevelDB


public object OpenPolicy {

    public val Open: Options.Open = LevelDBOptions.OpenPolicy(LevelDB.OpenPolicy.OPEN)
    public val OpenOrCreate: Options.Open = LevelDBOptions.OpenPolicy(LevelDB.OpenPolicy.OPEN_OR_CREATE)
    public val Create: Options.Open = LevelDBOptions.OpenPolicy(LevelDB.OpenPolicy.CREATE)

}
