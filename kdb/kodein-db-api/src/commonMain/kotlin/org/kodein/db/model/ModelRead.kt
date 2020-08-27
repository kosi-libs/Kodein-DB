package org.kodein.db.model

import org.kodein.db.*

public interface ModelRead : KeyMaker {

    public operator fun <M : Any> get(type: TKType<M>, key: Key<M>, vararg options: Options.Read): Sized<M>?

    public fun findAll(vararg options: Options.Read): ModelCursor<*>

    public fun <M : Any> findAllByType(type: TKType<M>, vararg options: Options.Read): ModelCursor<M>

    public fun <M : Any> findById(type: TKType<M>, id: Any, isOpen: Boolean = false, vararg options: Options.Read): ModelCursor<M>

    public fun <M : Any> findAllByIndex(type: TKType<M>, index: String, vararg options: Options.Read): ModelCursor<M>

    public fun <M : Any> findByIndex(type: TKType<M>, index: String, value: Any, isOpen: Boolean = false, vararg options: Options.Read): ModelCursor<M>

    public fun getIndexesOf(key: Key<*>, vararg options: Options.Read): Set<String>
}

@OptIn(ExperimentalStdlibApi::class)
public inline operator fun <reified M : Any> ModelRead.get(key: Key<M>, vararg options: Options.Read): Sized<M>? = get(tTypeOf(), key, *options)

public inline fun <reified M : Any> ModelRead.findAllByType(vararg options: Options.Read): ModelCursor<M> = findAllByType(tTypeOf(), *options)

public inline fun <reified M : Any> ModelRead.findById(id: Any, isOpen: Boolean = false, vararg options: Options.Read): ModelCursor<M> = findById(tTypeOf(), id, isOpen, *options)

public inline fun <reified M : Any> ModelRead.findAllByIndex(index: String, vararg options: Options.Read): ModelCursor<M> = findAllByIndex(tTypeOf(), index, *options)

public inline fun <reified M : Any> ModelRead.findByIndex(index: String, value: Any, isOpen: Boolean = false, vararg options: Options.Read): ModelCursor<M> = findByIndex(tTypeOf(), index, value, isOpen, *options)
