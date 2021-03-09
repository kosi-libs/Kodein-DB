package org.kodein.db.plugin.fts

import org.kodein.db.ExtensionKey
import org.kodein.db.Options


public class FtsQuery internal constructor(private val modelDB: FtsModelDB) {

    public fun searchAll(token: String, isOpen: Boolean, vararg options: Options.Read): FtsCursor<Any> =
        FtsCursor(modelDB, Any::class, token, isOpen, options)

//    public fun <M : Any> search(type: KClass<M>, token: String, field: String? = null): FtsCursor<M> {
//        TODO()
//    }

    public companion object : ExtensionKey<FtsQuery>
}
