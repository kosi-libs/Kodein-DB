package org.kodein.db.model.orm

import org.kodein.db.Ref
import org.kodein.memory.io.ReadBuffer

interface RefMapper {
    fun <M : Any> getRef(bytes: ReadBuffer): Ref<M>
    fun getBytes(ref: Ref<*>): ReadBuffer
}