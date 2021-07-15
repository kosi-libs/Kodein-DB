package org.kodein.db

import org.kodein.memory.Closeable
import kotlin.reflect.KClass


public interface Batch : DBWrite, Closeable {
    override fun <M : Any> put(model: M, vararg options: Options.Puts): Key<M> = put(model, *(options as Array<out Options.BatchPut>))
    public fun <M : Any> put(model: M, vararg options: Options.BatchPut): Key<M>

    override fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.Puts) { put(key, model, *(options as Array<out Options.BatchPut>)) }
    public fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.BatchPut)

    override fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.Deletes) { delete(type, key, *(options as Array<out Options.BatchDelete>)) }
    public fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.BatchDelete)

    public fun write(vararg options: Options.BatchWrite)
}

public inline fun <reified M : Any> Batch.delete(key: Key<M>, vararg options: Options.BatchDelete): Unit = delete(M::class, key, *options)

public inline fun <reified M : Any> Batch.deleteById(vararg id: Any, options: Array<out Options.BatchDelete> = emptyArray()): Unit = delete(keyById<M>(*id), *options)

public inline fun <reified M : Any> Batch.deleteAll(cursor: Cursor<M>, vararg options: Options.BatchDelete) {
    cursor.useKeys { seq -> seq.forEach { delete(it, *options) } }
}
