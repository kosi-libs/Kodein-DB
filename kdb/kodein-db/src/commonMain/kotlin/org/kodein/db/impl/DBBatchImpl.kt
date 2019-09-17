package org.kodein.db.impl

import org.kodein.db.BaseBatch
import org.kodein.db.DBBatch
import org.kodein.db.KeyMaker
import org.kodein.db.model.ModelBatch

class DBBatchImpl(override val mdb: ModelBatch) : DBBatch, DBWriteModule, KeyMaker by mdb, BaseBatch by mdb
