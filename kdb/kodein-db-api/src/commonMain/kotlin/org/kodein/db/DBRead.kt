@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package org.kodein.db

import kotlin.reflect.KClass

public interface DBRead : KeyMaker {

    public operator fun <M : Any> get(type: KClass<M>, key: Key<M>, vararg options: Options.Get): M?

    public fun findAll(vararg options: Options.Find): Cursor<*>

    public interface FindDsl<M : Any> {

        public fun all(): Cursor<M> = byId()

        public fun byId(vararg id: Any, isOpen: Boolean = false): Cursor<M>

        public fun byIndex(index: String, vararg value: Any, isOpen: Boolean = false): Cursor<M>
    }

    public fun <M : Any> find(type: KClass<M>, vararg options: Options.Find): FindDsl<M>

    public fun getIndexesOf(key: Key<*>): Set<String>

}

public inline operator fun <reified M : Any> DBRead.get(key: Key<M>, vararg options: Options.Get): M? = get(M::class, key, *options)

public inline fun <reified M : Any> DBRead.getById(vararg id: Any, options: Array<out Options.Get> = emptyArray()): M? = get(keyById(*id), *options)

public inline fun <reified M : Any> DBRead.find(vararg options: Options.Find): DBRead.FindDsl<M> = find(M::class, *options)
