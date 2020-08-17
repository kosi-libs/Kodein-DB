package org.kodein.db

import org.kodein.db.data.DataDB
import org.kodein.db.leveldb.LevelDB
import org.kodein.db.model.ModelDB

public typealias ModelMiddleware = ((ModelDB) -> ModelDB)
public typealias DataMiddleware = ((DataDB) -> DataDB)
public typealias LevelMiddleware = ((LevelDB) -> LevelDB)

public interface Middleware {

    public class Level(public val middleware: LevelMiddleware) : Options.Open
    public class Data(public val middleware: DataMiddleware) : Options.Open
    public class Model(public val middleware: ModelMiddleware) : Options.Open

}
