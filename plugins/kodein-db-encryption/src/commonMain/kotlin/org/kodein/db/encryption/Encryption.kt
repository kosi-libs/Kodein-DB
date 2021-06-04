package org.kodein.db.encryption

import org.kodein.db.DBListener
import org.kodein.db.Key
import org.kodein.db.Middleware
import org.kodein.db.Options
import org.kodein.db.data.DataDB
import org.kodein.db.model.ModelDB
import org.kodein.db.model.ModelDBListener
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.io.ReadMemory
import kotlin.reflect.KClass


public class Encryption(private val defaultOptions: EncryptOptions, private val byType: Map<KClass<*>, EncryptOptions> = emptyMap()) : Middleware.Model, Middleware.Data {

    public constructor(key: ReadMemory, byType: Map<KClass<*>, EncryptOptions> = emptyMap()) : this(EncryptOptions.Encrypt(key), byType)

    private var modelDB: ModelDB? = null
    private var typeOptions: Map<ReadMemory, EncryptOptions>? = null

    private val typeOptionsIdCache = HashMap<Int, EncryptOptions?>()

    override fun wrap(ddb: DataDB): DataDB =
        EncryptedDataDB(ddb, defaultOptions) {
            typeOptionsIdCache.getOrPut(it) {
                val to = typeOptions ?: error("Encryption middleware was not properly initialized: DataDB was wrapped but ModelDB was not")
                val mdb = modelDB ?: error("Encryption middleware was not properly initialized: DataDB was wrapped but ModelDB was not")
                val typeName = mdb.getTypeName(it)
                if (typeName != null) to[typeName] else null
            }
        }

    override fun wrap(mdb: ModelDB): ModelDB {
        modelDB = mdb

        //TODO: remove this once primitives are removed
        mdb.register(object : ModelDBListener<Any> {
            override fun willPut(model: Any, key: Key<Any>, typeName: ReadMemory, metadata: Metadata, options: Array<out Options.Puts>) {
                @Suppress("DEPRECATION_ERROR")
                if (model is org.kodein.db.model.Primitive) {
                    throw DBFeatureDisabledError("Primitives are deprecated and not supported with encryption.")
                }
            }
        })

        typeOptions = byType.mapKeys { mdb.typeTable.getTypeName(mdb.typeTable.getRootOf(it.key) ?: it.key) }
        return mdb
    }

}
