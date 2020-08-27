package org.kodein.db.model

import org.kodein.db.*
import kotlin.reflect.KClass

public interface ModelRead : KeyMaker {

    public operator fun <M : Any> get(type: KClass<M>, key: Key<M>, vararg options: Options.Read): Sized<M>?

    public fun findAll(vararg options: Options.Read): ModelCursor<*>

    public fun <M : Any> findAllByType(type: KClass<M>, vararg options: Options.Read): ModelCursor<M>

    public fun <M : Any> findById(type: KClass<M>, id: Any, isOpen: Boolean = false, vararg options: Options.Read): ModelCursor<M>

    public fun <M : Any> findAllByIndex(type: KClass<M>, index: String, vararg options: Options.Read): ModelCursor<M>

    public fun <M : Any> findByIndex(type: KClass<M>, index: String, value: Any, isOpen: Boolean = false, vararg options: Options.Read): ModelCursor<M>

    public fun getIndexesOf(key: Key<*>, vararg options: Options.Read): Set<String>
}

public inline operator fun <reified M : Any> ModelRead.get(key: Key<M>, vararg options: Options.Read): Sized<M>? = get(M::class, key, *options)

public inline fun <reified M : Any> ModelRead.findAllByType(vararg options: Options.Read): ModelCursor<M> = findAllByType(M::class, *options)

public inline fun <reified M : Any> ModelRead.findById(id: Any, isOpen: Boolean = false, vararg options: Options.Read): ModelCursor<M> = findById(M::class, id, isOpen, *options)

public inline fun <reified M : Any> ModelRead.findAllByIndex(index: String, vararg options: Options.Read): ModelCursor<M> = findAllByIndex(M::class, index, *options)

public inline fun <reified M : Any> ModelRead.findByIndex(index: String, value: Any, isOpen: Boolean = false, vararg options: Options.Read): ModelCursor<M> = findByIndex(M::class, index, value, isOpen, *options)
