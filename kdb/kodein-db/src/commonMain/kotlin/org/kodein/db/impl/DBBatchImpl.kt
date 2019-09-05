package org.kodein.db.impl

import org.kodein.db.BaseBatch
import org.kodein.db.DBBatch
import org.kodein.db.KeyMaker
import org.kodein.db.Options
import org.kodein.db.model.ModelBatch
import org.kodein.memory.Closeable

class DBBatchImpl(override val mdb: ModelBatch) : DBBatch, DBWriteBase, KeyMaker by mdb, BaseBatch by mdb
