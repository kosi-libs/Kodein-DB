package org.kodein.db.encryption

import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.db.data.DataCursor
import org.kodein.db.data.DataIndexCursor
import org.kodein.db.data.DataRead
import org.kodein.db.invoke
import org.kodein.db.toArrayMemory
import org.kodein.memory.crypto.*
import org.kodein.memory.io.ReadAllocation
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.asMemory


internal interface EncryptedDataReadModule : DataRead {

    val data: DataRead
    val eddb: EncryptedDataDB

    override fun get(key: ReadMemory, vararg options: Options.Get): ReadAllocation? {
        val value = data.get(key, *options) ?: return null
        return eddb.decrypt(key, value, options())
    }

    override fun findAll(vararg options: Options.Find): DataCursor =
        EncryptedDataCursor(eddb, data.findAll(*options), options)

    override fun findAllByType(type: Int, vararg options: Options.Find): DataCursor {
        eddb.encryptOptions(type) ?: return data.findAllByType(type, *options)
        return EncryptedDataCursor(eddb, data.findAllByType(type, *options), options)
    }

    override fun findById(type: Int, id: Value, isOpen: Boolean, vararg options: Options.Find): DataCursor {
        val encryptOptions = eddb.encryptOptions(type) ?: return data.findById(type, id, isOpen, *options)
        if (encryptOptions.hashDocumentID) throw DBFeatureDisabledError("Find by ID is disabled because ID hashing is enabled on this type.")
        return EncryptedDataCursor(eddb, data.findById(type, id, isOpen, *options), options)
    }

    override fun findAllByIndex(type: Int, index: String, vararg options: Options.Find): DataIndexCursor {
        eddb.encryptOptions(type) ?: return data.findAllByIndex(type, index, *options)
        return EncryptedDataIndexCursor(eddb, data.findAllByIndex(type, index, *options), options)
    }

    override fun findByIndex(type: Int, index: String, value: Value, isOpen: Boolean, vararg options: Options.Find): DataIndexCursor {
        val encryptOptions = eddb.encryptOptions(type) ?: return data.findByIndex(type, index, value, isOpen, *options)
        val hashValue = index in encryptOptions.hashIndexValues

        val actualValue = when {
            hashValue -> {
                if (isOpen) throw DBFeatureDisabledError("Searching by index value prefix is disabled because value hashing is enabled on index $index.")
                val macKey = Pbkdf2.withHmac(DigestAlgorithm.SHA256, encryptOptions.key, EncryptedDataDB.indexSalt, 1024, 32).asMemory()
                Value.of(DigestAlgorithm.SHA256.hmacOf(macKey, value.toArrayMemory()))
            }
            else -> value
        }
        return EncryptedDataIndexCursor(eddb, data.findByIndex(type, index, actualValue, isOpen, *options), options)
    }

    override fun getIndexesOf(key: ReadMemory): Set<String> = data.getIndexesOf(key)

}
