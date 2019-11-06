package org.kodein.db.impl.model

import org.kodein.db.*
import org.kodein.db.data.DataKeyMaker
import kotlin.reflect.KClass

internal interface ModelKeyMakerModule : KeyMaker {

    val mdb: ModelDBImpl
    val data: DataKeyMaker

    override fun <M : Any> newKey(type: KClass<M>, id: Value) = Key<M>(data.newKey(mdb.typeTable.getTypeName(type), id))

    override fun <M : Any> newKey(model: M, vararg options: Options.Write) = Key<M>(data.newKey(mdb.typeTable.getTypeName(model::class), mdb.getMetadata(model, options).id))
}
