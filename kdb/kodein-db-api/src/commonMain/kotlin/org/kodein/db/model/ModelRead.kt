package org.kodein.db.model

import org.kodein.db.*
import kotlin.reflect.KClass

interface ModelRead : KeyMaker {

    operator fun <M : Any> get(type: KClass<M>, key: Key<M>, vararg options: Options.Read): Sized<M>?

    fun findAll(vararg options: Options.Read): ModelCursor<*>

    fun <M : Any> findAllByType(type: KClass<M>, vararg options: Options.Read): ModelCursor<M>

    fun <M : Any> findById(type: KClass<M>, id: Value, isOpen: Boolean = false, vararg options: Options.Read): ModelCursor<M>

    fun <M : Any> findAllByIndex(type: KClass<M>, index: String, vararg options: Options.Read): ModelCursor<M>

    fun <M : Any> findByIndex(type: KClass<M>, index: String, value: Value, isOpen: Boolean = false, vararg options: Options.Read): ModelCursor<M>

    fun getIndexesOf(key: Key<*>, vararg options: Options.Read): List<String>
}

inline operator fun <reified M : Any> ModelRead.get(key: Key<M>, vararg options: Options.Read): Sized<M>? = get(M::class, key, *options)

inline fun <reified M : Any> ModelRead.findAllByType(vararg options: Options.Read) = findAllByType(M::class, *options)

inline fun <reified M : Any> ModelRead.findById(id: Value, isOpen: Boolean = false, vararg options: Options.Read) = findById(M::class, id, isOpen, *options)

inline fun <reified M : Any> ModelRead.findAllByIndex(index: String, vararg options: Options.Read) = findAllByIndex(M::class, index, *options)

inline fun <reified M : Any> ModelRead.findByIndex(index: String, value: Value, isOpen: Boolean = false, vararg options: Options.Read) = findByIndex(M::class, index, value, isOpen, *options)
