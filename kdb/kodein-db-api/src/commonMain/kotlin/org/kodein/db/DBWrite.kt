package org.kodein.db

interface DBWrite : KeyMaker {

    fun put(model: Any, vararg options: Options.Write): Int

    fun <M : Any> putAndGetHeapKey(model: M, vararg options: Options.Write): Key<M>
    fun <M : Any> putAndGetNativeKey(model: M, vararg options: Options.Write): Key.Native<M>

    fun delete(key: Key<*>, vararg options: Options.Write)

}
