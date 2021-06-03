package org.kodein.db

import kotlin.reflect.KClass

public interface KeyMaker {

    public fun <M : Any> keyById(type: KClass<M>, vararg id: Any): Key<M>

    public fun <M : Any> keyFrom(model: M, vararg options: Options.Puts): Key<M>

    public fun <M : Any> keyFromB64(type: KClass<M>, b64: String): Key<M>
}

public inline fun <reified M : Any> KeyMaker.keyById(vararg id: Any): Key<M> = keyById(M::class, *id)

public inline fun <reified M : Any> KeyMaker.keyFromB64(b64: String): Key<M> = keyFromB64(M::class, b64)
