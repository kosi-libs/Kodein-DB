package org.kodein.db.impl

import org.kodein.db.DBWrite
import org.kodein.db.Key
import org.kodein.db.Options
import org.kodein.db.model.ModelWrite

internal interface BaseDBWrite : DBWrite, BaseDBBase {

    override val mdb: ModelWrite

    override fun put(model: Any, vararg options: Options.Write): Int = mdb.put(model, *options)

    override fun <M : Any> putAndGetHeapKey(model: M, vararg options: Options.Write): Key<M> = mdb.putAndGetHeapKey(model, *options).value

    override fun <M : Any> putAndGetNativeKey(model: M, vararg options: Options.Write): Key.Native<M> = mdb.putAndGetNativeKey(model, *options).value

    override fun delete(key: Key<*>, vararg options: Options.Write) = mdb.delete(key, *options)
}
