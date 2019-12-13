package org.kodein.db.impl

import org.kodein.db.Key
import org.kodein.db.KeyMaker
import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.db.model.ModelKeyMaker
import kotlin.reflect.KClass

internal interface KeyMakerModule : KeyMaker {

    val mdb: ModelKeyMaker

    override fun <M : Any> newKey(type: KClass<M>, vararg id: Any): Key<M> = mdb.newKey(type, Value.ofAll(*id))

    override fun <M : Any> newKeyFrom(model: M, vararg options: Options.Write): Key<M> = mdb.newKeyFrom(model, *options)
}
