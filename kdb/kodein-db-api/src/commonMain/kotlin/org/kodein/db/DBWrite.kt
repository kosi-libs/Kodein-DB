package org.kodein.db

import kotlin.reflect.KClass

public interface DBWrite : KeyMaker {
    public fun <M : Any> put(model: M): Key<M> = put(model, *emptyArray())
    public fun <M : Any> put(model: M, vararg options: Options.Puts): Key<M>

    public fun <M : Any> put(key: Key<M>, model: M) { put(key, model, *emptyArray()) }
    public fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.Puts)

    public fun <M : Any> delete(type: KClass<M>, key: Key<M>) { delete(type, key, *emptyArray()) }
    public fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.Deletes)
}

public inline fun <reified M : Any> DBWrite.delete(key: Key<M>): Unit = delete(key, *emptyArray())
public inline fun <reified M : Any> DBWrite.delete(key: Key<M>, vararg options: Options.Deletes): Unit = delete(M::class, key, *options)

public inline fun <reified M : Any> DBWrite.deleteById(vararg id: Any): Unit = deleteById<M>(*id, options = emptyArray())
public inline fun <reified M : Any> DBWrite.deleteById(vararg id: Any, options: Array<out Options.Deletes> = emptyArray()): Unit = delete(keyById(*id), *options)

public inline fun <reified M : Any> DBWrite.deleteAll(cursor: Cursor<M>) { deleteAll(cursor, *emptyArray()) }
public inline fun <reified M : Any> DBWrite.deleteAll(cursor: Cursor<M>, vararg options: Options.Deletes) {
    cursor.useKeys { seq -> seq.forEach { delete(it, *options) } }
}
