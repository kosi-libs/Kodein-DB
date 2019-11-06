package org.kodein.db.impl

import org.kodein.db.DBWrite
import org.kodein.db.Key
import org.kodein.db.Options
import org.kodein.db.model.ModelWrite
import kotlin.reflect.KClass

internal interface DBWriteModule : DBWrite {

    val mdb: ModelWrite

    override fun <M : Any> put(model: M, vararg options: Options.Write) = mdb.put(model, *options).key
    override fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.Write) { mdb.put(key, model, *options) }

    override fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.Write) = mdb.delete(type, key, *options)
}
