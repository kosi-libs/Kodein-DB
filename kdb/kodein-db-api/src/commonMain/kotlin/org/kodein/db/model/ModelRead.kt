package org.kodein.db.model

import org.kodein.db.*
import kotlin.reflect.KClass

interface ModelRead : KeyMaker {

    operator fun <M : Any> get(type: KClass<M>, key: Key<M>, vararg options: Options.Read): Sized<M>?

    fun findAll(vararg options: Options.Read): ModelCursor<*>

    fun <M : Any> findAllByType(type: KClass<M>, vararg options: Options.Read): ModelCursor<M>

    fun <M : Any> findByPrimaryKey(type: KClass<M>, primaryKey: Value, isOpen: Boolean = false, vararg options: Options.Read): ModelCursor<M>

    fun <M : Any> findAllByIndex(type: KClass<M>, name: String, vararg options: Options.Read): ModelCursor<M>

    fun <M : Any> findByIndex(type: KClass<M>, name: String, value: Value, isOpen: Boolean = false, vararg options: Options.Read): ModelCursor<M>

    fun getIndexesOf(key: Key<*>, vararg options: Options.Read): List<String>
}

inline operator fun <reified M : Any> ModelRead.get(key: Key<M>, vararg options: Options.Read): Sized<M>? = get(M::class, key, *options)

inline fun <reified M : Any> ModelRead.findAllByType(vararg options: Options.Read) = findAllByType(M::class, *options)

inline fun <reified M : Any> ModelRead.findByPrimaryKey(primaryKey: Value, isOpen: Boolean = false, vararg options: Options.Read) = findByPrimaryKey(M::class, primaryKey, isOpen, *options)

inline fun <reified M : Any> ModelRead.findAllByIndex(name: String, vararg options: Options.Read) = findAllByIndex(M::class, name, *options)

inline fun <reified M : Any> ModelRead.findByIndex(name: String, value: Value, isOpen: Boolean = false, vararg options: Options.Read) = findByIndex(M::class, name, value, isOpen, *options)
