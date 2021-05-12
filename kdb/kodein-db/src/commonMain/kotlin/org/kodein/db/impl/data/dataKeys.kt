@file:OptIn(ExperimentalUnsignedTypes::class)

package org.kodein.db.impl.data

import org.kodein.db.Body
import org.kodein.db.Value
import org.kodein.db.data.DataIndexMap
import org.kodein.db.writeBody
import org.kodein.memory.io.*
import org.kodein.memory.text.Charset
import org.kodein.memory.text.readNullTerminatedString
import org.kodein.memory.text.sizeOf
import org.kodein.memory.text.writeNullTerminatedString
import org.kodein.memory.use


/*
    Documents:
        (documentKey = 'o' 0 type id 0) -> document

    Indexes:
        v0: (indexKey = 'i' 0 type name 0 value 0 id 0) -> documentKey
            First bit is always 0 since a document key always starts with 'o', which is encoded b01101111
        v1: (indexKey = 'i' 0 type name 0 value 0 id 0) -> 128 value.size:UShort id.size:UShort associatedData
            128 is the version number (b10000000). Its first bit is 1 to differentiate from v0.

    References:
        v0: (refKey = 'r' 0 type id 0) -> (indexKey.size:Int indexKey)+
            First bit is always 0 since indexKey.size is coded as a 4 bytes signed Int, and an index key size is always positive.
        v1: (refKey = 'r' 0 type id 0) -> 128 (indexName 0 valuesSize:Int (value.size:UShort value)+)+
            128 is the version number (b10000000). Its first bit is 1 to differentiate from v0.
*/

private object Prefix {
    const val DOCUMENT = 'o'.toByte()
    const val INDEX = 'i'.toByte()
    const val REFERENCE = 'r'.toByte()
}

internal const val NULL = 0.toByte()

internal fun ReadMemory.verifyDocumentKey() {
    require(get(0) == Prefix.DOCUMENT) { "Bad key" }
    require(get(1) == NULL) { "Bad key" }
    require(firstIndexOf(NULL, 2) > 0) { "Bad key" }
}

internal val emptyDocumentPrefix = byteArrayOf(Prefix.DOCUMENT, NULL)

internal fun Writeable.writeDocumentKey(type: Int, id: Value?, isOpen: Boolean = false) {
    writeByte(Prefix.DOCUMENT)
    writeByte(NULL)

    writeInt(type)

    if (id != null) {
        writeBody(id)
        if (!isOpen) writeByte(NULL)
    }
}

internal fun getDocumentKeySize(id: Value?, isOpen: Boolean = false): Int {
    var size = (
            2              // 'o' 0
        +   4)             // type

    if (id != null) {
        size += id.size    // id
        if (!isOpen)
            size += 1      // 0
    }

    return size
}

internal fun getDocumentKeyType(key: ReadMemory): Int {
    return key.getInt(2)
}

internal fun getDocumentKeyID(key: ReadMemory): ReadMemory {
    return key.slice(6, key.size - 7)
}



internal fun Writeable.writeIndexKey(type: Int, id: ReadMemory, name: String, value: Value) {
    writeIndexKeyStart(type, name, value)
    writeBytes(id)
    writeByte(NULL)
}

internal fun getIndexKeySize(documentId: ReadMemory, name: String, value: Value): Int {
    return (
            2                              // 'i' 0
        +   4                              // type
        +   Charset.UTF8.sizeOf(name) + 1  // name 0
        +   value.size + 1                 // value 0
        +   documentId.size + 1            // id 0
    )
}

@Suppress("DuplicatedCode")
internal fun Writeable.writeIndexKeyStart(type: Int, name: String, value: Value?, isOpen: Boolean = false) {
    writeByte(Prefix.INDEX)
    writeByte(NULL)

    writeInt(type)

    writeNullTerminatedString(name, Charset.UTF8)

    if (value != null) {
        writeBody(value)
        if (!isOpen)
            writeByte(NULL)
    }
}

internal fun getIndexKeyStartSize(name: String, value: Value?, isOpen: Boolean = false): Int {
    var size = (
            2                              // 'i' 0
        +   4                              // type
        +   Charset.UTF8.sizeOf(name) + 1  // name 0
    )

    if (value != null) {
        size += value.size                 // value
        if (!isOpen)
            size += 1                      // 0
    }

    return size
}

internal fun Writeable.writeIndexBody(documentId: ReadMemory, value: Value, associatedData: Body?) {
    check(documentId.size < UShort.MAX_VALUE.toInt()) { "Document ID too big (must be max ${UShort.MAX_VALUE} bytes)" }
    check(value.size < UShort.MAX_VALUE.toInt()) { "Index value too big (must be max ${UShort.MAX_VALUE} bytes)" }
    writeUByte(128u)
    writeUShort(value.size.toUShort())
    writeUShort(documentId.size.toUShort())
    if (associatedData != null) writeBody(associatedData)
}

internal fun getIndexKeyDocumentType(key: ReadMemory): Int {
    return key.getInt(2)
}

internal fun getIndexKeyName(key: ReadMemory): String {
    return key.sliceAt(6).asReadable().readNullTerminatedString(Charset.UTF8)
}

internal fun getIndexDocumentId(key: ReadMemory, body: ReadMemory): ReadMemory {
    val size = when (body.getUByte(0)) {
        in 0u..127u -> { // v0
            body.size - 7
        }
        128u.toUByte() -> { // v1
            body.getUShort(3).toInt()
        }
        else -> error("Unknown version. Are you trying to read a DB that was created with a newer version of Kodein-DB?")
    }
    return key.slice(key.size - 1 - size, size)
}

internal fun getIndexBodyAssociatedData(body: ReadMemory): ReadMemory? {
    return when (body.getUByte(0)) {
        in 0u..127u -> { // v0
            null
        }
        128u.toUByte() -> { // v1
            if (body.size == 5) null
            else body.sliceAt(5)
        }
        else -> error("Unknown version. Are you trying to read a DB that was created with a newer version of Kodein-DB?")
    }
}



internal fun Writeable.writeRefKeyFromDocumentKey(documentKey: ReadMemory) {
    writeByte(Prefix.REFERENCE)
    writeBytes(documentKey.sliceAt(1))
}

internal fun Writeable.writeRefBody(indexes: DataIndexMap) {
    writeUByte(128u)
    indexes.forEach { (name, data) ->
        writeNullTerminatedString(name)
        val valuesSize = data.sumOf { (value, _) -> 2 + value.size }
        writeInt(valuesSize)
        data.forEach { (value, _) ->
            check(value.size < UShort.MAX_VALUE.toInt()) { "Index value too big (must be max ${UShort.MAX_VALUE} bytes)" }
            writeUShort(value.size.toUShort())
            writeBody(value)
        }
    }
}

internal fun getRefBodySize(indexes: DataIndexMap): Int {
    var size = 1                               // 128
    indexes.forEach { (name, data) ->          // (
        size += Charset.UTF8.sizeOf(name) + 1  // indexName 0
        size += 4                              // valuesSize:Int
        data.forEach { (value, _) ->           // (
            size += 2                          // value.size:UShort
            size += value.size                 // value
        }                                      // )+
    }                                          // )+
    return size
}

@OptIn(ExperimentalStdlibApi::class)
internal fun getRefBodyIndexNames(body: ReadMemory): Set<String> {
    return when (body.getUByte(0)) {
        in 0u..127u -> { // v0
            val r = body.asReadable()
            buildSet {
                while (r.valid()) {
                    val length = r.readInt()
                    val indexKey = r.readSlice(length)
                    add(getIndexKeyName(indexKey))
                }
            }
        }
        128u.toUByte() -> { // v1
            val r = body.asReadable()
            r.skip(1)
            buildSet {
                while (r.valid()) {
                    val indexName = r.readNullTerminatedString(Charset.UTF8)
                    val valuesSize = r.readInt()
                    add(indexName)
                    r.skip(valuesSize)
                }
            }
        }
        else -> error("Unknown version. Are you trying to read a DB that was created with a newer version of Kodein-DB?")
    }
}

@OptIn(ExperimentalStdlibApi::class)
internal fun getRefIndexKeys(key: ReadMemory, body: ReadMemory): Sequence<ReadMemory> {
    return when (body.getUByte(0)) {
        in 0u..127u -> { // v0
            val r = body.asReadable()
            sequence {
                while (r.valid()) {
                    val size = r.readInt()
                    yield(r.readSlice(size))
                }
            }
        }
        128u.toUByte() -> { // v1
            // RefKey & DocumentKey are the same
            val documentType = getDocumentKeyType(key)
            val documentId = getDocumentKeyID(key)

            val rBody = body.asReadable()
            rBody.skip(1)

            sequence {
                while (rBody.valid()) {
                    val name = rBody.readNullTerminatedString(Charset.UTF8)

                    val valuesSize = rBody.readInt()
                    val rValues = rBody.readSlice(valuesSize).asReadable()
                    while (rValues.valid()) {
                        val valueSize = rValues.readUShort().toInt()
                        val value = Value.of(rValues.readSlice(valueSize))
                        Allocation.native(getIndexKeySize(documentId, name, value)) { writeIndexKey(documentType, documentId, name, value) }
                            .use { yield(it) }
                    }
                }
            }
        }
        else -> error("Unknown version. Are you trying to read a DB that was created with a newer version of Kodein-DB?")
    }
}
