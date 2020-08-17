package org.kodein.db

import kotlin.reflect.KClass

public interface KeyMaker {

    public fun <M : Any> newKey(type: KClass<M>, vararg id: Any): Key<M>

    public fun <M : Any> newKeyFrom(model: M, vararg options: Options.Write): Key<M>

    public fun <M : Any> newKeyFromB64(type: KClass<M>, b64: String): Key<M>
}

public inline fun <reified M : Any> KeyMaker.newKey(vararg id: Any): Key<M> = newKey(M::class, *id)
public inline fun <reified M : Any> KeyMaker.newKeyFromB64(b64: String): Key<M> = newKeyFromB64(M::class, b64)
