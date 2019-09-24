package org.kodein.db.impl

import org.kodein.db.BaseBatch
import org.kodein.db.Batch
import org.kodein.db.KeyMaker
import org.kodein.db.model.ModelBatch

class BatchImpl(override val mdb: ModelBatch) : Batch, DBWriteModule, KeyMaker by mdb, BaseBatch by mdb
