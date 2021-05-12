package org.kodein.db.impl.data

import org.kodein.db.DBFactory
import org.kodein.db.Middleware
import org.kodein.db.Options
import org.kodein.db.all
import org.kodein.db.data.DataDB
import org.kodein.db.kv.KeyValueDB
import org.kodein.db.kv.LevelDBOptions
import org.kodein.db.leveldb.LevelDBFactory

public abstract class AbstractDataDBFactory : DBFactory<DataDB> {

    protected abstract val kvdbFactory: DBFactory<KeyValueDB>

    override fun open(path: String, vararg options: Options.Open): DataDB {
        val dataMiddlewares = options.all<Middleware.Data>()

        return dataMiddlewares.fold(
            DataDBImpl(kvdbFactory.open(path, *options)) as DataDB
        ) { ddb, middleware -> middleware.wrap(ddb) }
    }

    override fun destroy(path: String, vararg options: Options.Open) {
        kvdbFactory.destroy(path, *options)
    }
}
