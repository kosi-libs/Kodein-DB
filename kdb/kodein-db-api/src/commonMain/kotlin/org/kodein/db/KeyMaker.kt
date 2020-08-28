package org.kodein.db

import kotlin.reflect.KClass

public interface KeyMaker {

    public fun <M : Any> key(type: KClass<M>, vararg id: Any): Key<M>

    public fun <M : Any> keyFrom(model: M, vararg options: Options.Write): Key<M>

    public fun <M : Any> keyFromB64(type: KClass<M>, b64: String): Key<M>
}

public inline fun <reified M : Any> KeyMaker.key(vararg id: Any): Key<M> = key(M::class, *id)
public inline fun <reified M : Any> KeyMaker.keyFromB64(b64: String): Key<M> = keyFromB64(M::class, b64)
