package org.kodein.db.encryption

import org.kodein.db.*
import org.kodein.db.data.*
import org.kodein.db.kv.KeyValueDB
import org.kodein.memory.crypto.*
import org.kodein.memory.io.*
import org.kodein.memory.text.array
import org.kodein.memory.text.readString
import org.kodein.memory.use
import org.kodein.memory.util.secure
import kotlin.random.Random


internal class EncryptedDataDB(override val data: DataDB, private val defaultOptions: EncryptOptions, private val getTypeOptions: (Int) -> EncryptOptions?) : DataDB, EncryptedDataReadModule {

    companion object {
        internal val documentIdSalt = Memory.array("DocumentID")
        internal val indexSalt = Memory.array("Index")
    }

    override val kv: KeyValueDB get() = data.kv
    override val eddb: EncryptedDataDB get() = this

    internal fun encryptOptions(type: Int): EncryptOptions.Encrypt? =
        when (val options = getTypeOptions(type) ?: defaultOptions) {
            is EncryptOptions.KeepPlain -> null
            is EncryptOptions.Encrypt -> options
        }

    internal fun encrypt(key: ReadMemory, body: Body, indexes: DataIndexMap, docKeyOption: EncryptedDocumentKey?, ivOption: ReadMemory?): Triple<Body, DataIndexMap, Memory?> {
        val options = encryptOptions(keyType(key)) ?: return Triple(body, indexes, null)
        val encryptKey = docKeyOption?.key ?: options.key

        val aesKey = Pbkdf2.withHmac(DigestAlgorithm.SHA256, encryptKey, key, 1024, 32).asMemory()

        val newBody = Body {
            val aesIV = ivOption ?: Random.secure().nextBytes(16).asMemory()
            it.writeBytes(aesIV)
            AES128.encrypt(CipherMode.CBC(aesIV), aesKey, it) {
                body.writeInto(this)
            }
        }

        var macKey: ByteArray? = null
        try {
            val hmacDigest by lazy {
                macKey = Pbkdf2.withHmac(DigestAlgorithm.SHA256, options.key, indexSalt, 1024, 32)
                DigestWriteable.newHmacInstance(DigestAlgorithm.SHA256, macKey!!.asMemory())
            }
            val newIndexes = indexes.map { (name, values) ->
                val newValues = values.map { (value, associatedData) ->
                    val newValue = when {
                        name in options.hashIndexValues -> {
                            hmacDigest.reset()
                            value.writeInto(hmacDigest)
                            Value.of(hmacDigest.digestBytes())
                        }
                        else -> value
                    }
                    val newAssociatedData = when {
                        associatedData == null -> null
                        else -> {
                            Body {
                                val aesIV = ivOption ?: Random.secure().nextBytes(16).asMemory()
                                it.writeBytes(aesIV)
                                AES128.encrypt(CipherMode.CBC(aesIV), aesKey, it) {
                                    associatedData.writeInto(this)
                                }
                            }
                        }
                    }
                    newValue to newAssociatedData
                }
                name to newValues
            }.toMap()
            return Triple(newBody, newIndexes, aesKey)
        } finally {
            macKey?.fill(0)
        }
    }

    internal fun decrypt(key: ReadMemory, value: ReadAllocation, docKeyOption: EncryptedDocumentKey?): ReadAllocation {
        val options = encryptOptions(keyType(key)) ?: return value
        val encryptKey = docKeyOption?.key ?: options.key

        val aesKey = Pbkdf2.withHmac(DigestAlgorithm.SHA256, encryptKey, key, 1024, 32).asMemory()

        val cipherText = value.asReadable()
        val iv = cipherText.readBytes(16).asMemory()

        val clearText = Allocation.native(cipherText.remaining) {
            AES128.decrypt(CipherMode.CBC(iv), aesKey, this).use {
                it.writeBytes(cipherText)
            }
        }
        value.close()

        return clearText
    }

    override fun newKey(type: Int, id: Value): ReadMemory {
        val options = encryptOptions(type) ?: return data.newKey(type, id)
        if (!options.hashDocumentID) return data.newKey(type, id)
        val macKey = Pbkdf2.withHmac(DigestAlgorithm.SHA256, options.key, documentIdSalt, 1024, 32)
        val hash = DigestAlgorithm.SHA256.hmacOf(macKey.asMemory(), id.toArrayMemory())
        macKey.fill(0)
        return data.newKey(type, Value.of(hash))
    }

    override fun keyType(key: ReadMemory): Int = data.keyType(key)

    override fun put(key: ReadMemory, body: Body, indexes: DataIndexMap, vararg options: Options.DirectPut): Int {
        val (newBody, newIndexes, aesKey) = encrypt(key, body, indexes, options(), options<IV>()?.iv)
        val size = data.put(key, newBody, newIndexes, *options)
        aesKey?.fill(0)
        return size
    }

    override fun delete(key: ReadMemory, vararg options: Options.DirectDelete) = data.delete(key, *options)

    override fun newBatch(vararg options: Options.NewBatch): DataBatch =
        EncryptedDataBatch(this, data.newBatch(*options))

    override fun newSnapshot(vararg options: Options.NewSnapshot): DataSnapshot =
        EncryptedDataSnapshot(this, data.newSnapshot(*options))

    override fun close() {
        data.close()
    }

    override fun <T : Any> getExtension(key: ExtensionKey<T>): T? = data.getExtension(key)
}
