package org.kodein.db.impl.data

import org.kodein.db.Middleware
import org.kodein.db.Options
import org.kodein.db.all
import org.kodein.db.data.DataDB
import org.kodein.db.data.DataDBFactory
import org.kodein.db.invoke
import org.kodein.db.ldb.LevelDBOptions
import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.LevelDBFactory

abstract class AbstractDataDBFactory : DataDBFactory {

    protected abstract val ldbFactory: LevelDBFactory

    override fun open(path: String, vararg options: Options.Open): DataDB{
        val levelMiddlewares = options.all<Middleware.Level>().map { it.middleware }
        val dataMiddlewares = options.all<Middleware.Data>().map { it.middleware }

        val ldb = levelMiddlewares.fold(ldbFactory.open(path, options<LevelDBOptions>()?.options ?: LevelDB.Options.DEFAULT)) { ldb, middleware -> middleware(ldb) }

        return dataMiddlewares.fold(DataDBImpl(ldb) as DataDB) { ddb, middleware -> middleware(ddb) }
    }

    override fun destroy(path: String, vararg options: Options.Open) {
        ldbFactory.destroy(path, options<LevelDBOptions>()?.options ?: LevelDB.Options.DEFAULT)
    }
}
