package org.kodein.db

import kotlin.reflect.KClass

interface DBWrite : KeyMaker {

    fun put(model: Any, vararg options: Options.Write): Int

    fun <M : Any> putAndGetHeapKey(model: M, vararg options: Options.Write): Key<M>
    fun <M : Any> putAndGetNativeKey(model: M, vararg options: Options.Write): Key.Native<M>

    fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.Write)

}
