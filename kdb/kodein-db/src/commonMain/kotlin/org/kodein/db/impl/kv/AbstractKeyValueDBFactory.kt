package org.kodein.db.impl.kv

import org.kodein.db.DBFactory
import org.kodein.db.Middleware
import org.kodein.db.Options
import org.kodein.db.all
import org.kodein.db.kv.KeyValueDB
import org.kodein.db.kv.LevelDBOptions
import org.kodein.db.leveldb.LevelDBFactory


public abstract class AbstractKeyValueDBFactory : DBFactory<KeyValueDB> {

    protected abstract val ldbFactory: LevelDBFactory

    override fun open(path: String, vararg options: Options.Open): KeyValueDB {
        val kvMiddlewares = options.all<Middleware.KeyValue>()

        return kvMiddlewares.fold(
            KeyValueDBImpl(ldbFactory.open(path, LevelDBOptions.from(options))) as KeyValueDB
        ) { kvdb, middleware -> middleware.wrap(kvdb) }
    }

    override fun destroy(path: String, vararg options: Options.Open) {
        ldbFactory.destroy(path, LevelDBOptions.from(options))
    }

}
