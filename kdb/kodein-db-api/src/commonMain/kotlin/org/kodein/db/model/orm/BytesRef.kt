package org.kodein.db.model.orm

import org.kodein.db.Ref
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.ReadBuffer
import org.kodein.memory.io.getBytesHere
import org.kodein.memory.io.wrap


data class BytesRef<out T : Any>(val bytes: ByteArray) : Ref<T> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BytesRef<*>

        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }
}

open class BytesRefMapper : RefMapper {

    override fun <M : Any> getRef(bytes: ReadBuffer) = BytesRef<M>(bytes.getBytesHere())

    override fun getBytes(ref: Ref<*>) = KBuffer.wrap((ref as BytesRef<*>).bytes)

    companion object {
        val instance = BytesRefMapper()
    }
}
