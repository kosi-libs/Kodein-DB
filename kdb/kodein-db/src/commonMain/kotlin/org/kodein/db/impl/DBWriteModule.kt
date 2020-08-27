package org.kodein.db.impl

import org.kodein.db.DBWrite
import org.kodein.db.Key
import org.kodein.db.Options
import org.kodein.db.TKType
import org.kodein.db.model.ModelWrite
import kotlin.reflect.KClass

internal interface DBWriteModule : DBWrite {

    val mdb: ModelWrite

    override fun <M : Any> put(type: TKType<M>, model: M, vararg options: Options.Write) = mdb.put(type, model, *options).key
    override fun <M : Any> put(type: TKType<M>, key: Key<M>, model: M, vararg options: Options.Write) { mdb.put(type, key, model, *options) }

    override fun <M : Any> delete(type: TKType<M>, key: Key<M>, vararg options: Options.Write) = mdb.delete(type, key, *options)
}
