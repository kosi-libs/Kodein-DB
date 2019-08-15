package org.kodein.db

import org.kodein.db.data.DataDB
import org.kodein.db.leveldb.LevelDB
import org.kodein.db.model.ModelDB

typealias ModelMiddleware = ((ModelDB) -> ModelDB)
typealias DataMiddleware = ((DataDB) -> DataDB)
typealias LevelMiddleware = ((LevelDB) -> LevelDB)

interface Middleware {

    class Level(val middleware: LevelMiddleware) : Options.Open
    class Data(val middleware: DataMiddleware) : Options.Open
    class Model(val middleware: ModelMiddleware) : Options.Open

}
