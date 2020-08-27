package org.kodein.db

import kotlin.reflect.KClass

public interface DBWrite : KeyMaker {
    public fun <M : Any> put(type: TKType<M>, model: M, vararg options: Options.Write): Key<M>
    public fun <M : Any> put(type: TKType<M>, key: Key<M>, model: M, vararg options: Options.Write)

    public fun <M : Any> delete(type: TKType<M>, key: Key<M>, vararg options: Options.Write)
}

public inline fun <reified M : Any> DBWrite.put(model: M, vararg options: Options.Write): Key<M> = put(tTypeOf(), model, *options)
public inline fun <reified M : Any> DBWrite.put(key: Key<M>, model: M, vararg options: Options.Write): Unit = put(tTypeOf(), key, model, *options)

public inline fun <reified M : Any> DBWrite.delete(key: Key<M>, vararg options: Options.Write): Unit = delete(tTypeOf(), key, *options)
