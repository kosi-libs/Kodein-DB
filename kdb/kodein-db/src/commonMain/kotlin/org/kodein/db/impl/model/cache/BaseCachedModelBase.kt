package org.kodein.db.impl.model.cache

import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.db.model.Key
import org.kodein.db.model.ModelBase
import kotlin.reflect.KClass

interface BaseCachedModelBase : ModelBase {

    val mdb: ModelBase

    override fun <M : Any> getHeapKey(type: KClass<M>, primaryKey: Value): Key<M> = mdb.getHeapKey(type, primaryKey)
    override fun <M : Any> getNativeKey(type: KClass<M>, primaryKey: Value): Key.Native<M> = mdb.getNativeKey(type, primaryKey)

    override fun <M : Any> getHeapKey(model: M, vararg options: Options.Write): Key<M> = mdb.getHeapKey(model, *options)
    override fun <M : Any> getNativeKey(model: M, vararg options: Options.Write): Key.Native<M> = mdb.getNativeKey(model, *options)

}
