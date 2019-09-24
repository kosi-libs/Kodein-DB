package org.kodein.db

import kotlin.reflect.KClass

interface AsyncDBWrite : KeyMaker {

    suspend fun put(model: Any, vararg options: Options.Write): Int

    suspend fun <M : Any> putAndGetHeapKey(model: M, vararg options: Options.Write): Key<M>
    suspend fun <M : Any> putAndGetNativeKey(model: M, vararg options: Options.Write): Key.Native<M>

    suspend fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.Write)

    fun sync(): DBWrite
}

suspend inline fun <reified M : Any> AsyncDBWrite.delete(key: Key<M>, vararg options: Options.Write) = delete(M::class, key, *options)
