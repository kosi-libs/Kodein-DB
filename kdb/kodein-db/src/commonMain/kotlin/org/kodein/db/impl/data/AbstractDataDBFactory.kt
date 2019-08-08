package org.kodein.db.impl.data

import org.kodein.db.Options
import org.kodein.db.data.DataDB
import org.kodein.db.data.DataDBFactory
import org.kodein.db.invoke
import org.kodein.db.ldb.LevelDBOptions
import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.LevelDBFactory

abstract class AbstractDataDBFactory : DataDBFactory {

    protected abstract val ldbFactory: LevelDBFactory

    override fun open(path: String, vararg options: Options.Open): DataDB{
        val opt: LevelDBOptions? = options()
        return DataDBImpl(ldbFactory.open(path, opt?.options ?: LevelDB.Options.DEFAULT))
    }

    override fun destroy(path: String, vararg options: Options.Open) {
        val opt: LevelDBOptions? = options()
        ldbFactory.destroy(path, opt?.options ?: LevelDB.Options.DEFAULT)
    }
}
