package org.kodein.db

import kotlin.reflect.KClass

interface KeyMaker {

    fun <M : Any> getHeapKey(type: KClass<M>, primaryKey: Value): Key<M>
    fun <M : Any> getNativeKey(type: KClass<M>, primaryKey: Value): Key.Native<M>

    fun <M : Any> getHeapKey(model: M, vararg options: Options.Write): Key<M>
    fun <M : Any> getNativeKey(model: M, vararg options: Options.Write): Key.Native<M>

    fun <M : Any> getRef(key: Key<M>): Ref<M>

    fun <M : Any> getHeapKey(type: KClass<M>, ref: Ref<M>): Key<M>
    fun <M : Any> getNativeKey(type: KClass<M>, ref: Ref<M>): Key.Native<M>
}

inline fun <reified M : Any> KeyMaker.getHeapKey(primaryKey: Value) = getHeapKey(M::class, primaryKey)
inline fun <reified M : Any> KeyMaker.getNativeKey(primaryKey: Value) = getNativeKey(M::class, primaryKey)

inline fun <reified M : Any> KeyMaker.getHeapKey(ref: Ref<M>) = getHeapKey(M::class, ref)
inline fun <reified M : Any> KeyMaker.getNativeKey(ref: Ref<M>) = getNativeKey(M::class, ref)
