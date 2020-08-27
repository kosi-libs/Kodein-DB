package org.kodein.db

import kotlin.reflect.KType
import kotlin.reflect.typeOf

public interface KeyMaker {

    public fun <M : Any> newKey(type: TKType<M>, vararg id: Any): Key<M>

    public fun <M : Any> newKeyFrom(type: TKType<M>, model: M, vararg options: Options.Write): Key<M>

    public fun <M : Any> newKeyFromB64(type: TKType<M>, b64: String): Key<M>
}

@OptIn(ExperimentalStdlibApi::class)
public inline fun <reified M : Any> KeyMaker.newKey(vararg id: Any): Key<M> = newKey(tTypeOf(), *id)
@OptIn(ExperimentalStdlibApi::class)
public inline fun <reified M : Any> KeyMaker.newKeyFrom(model: M, vararg options: Options.Write): Key<M> = newKeyFrom(tTypeOf(), model, *options)
@OptIn(ExperimentalStdlibApi::class)
public inline fun <reified M : Any> KeyMaker.newKeyFromB64(b64: String): Key<M> = newKeyFromB64(tTypeOf(), b64)
