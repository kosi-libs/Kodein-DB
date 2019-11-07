@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package org.kodein.db

import kotlin.reflect.KClass

interface DBRead : KeyMaker {

    operator fun <M : Any> get(type: KClass<M>, key: Key<M>, vararg options: Options.Read): M?

    fun findAll(vararg options: Options.Read): Cursor<*>

    interface FindDsl<M : Any> {

        interface ByDsl<M : Any> {

            fun all(): Cursor<M>

            fun withValue(value: Value, isOpen: Boolean = false): Cursor<M>
        }

        fun all(): Cursor<M> = byId().all()

        fun byId(): ByDsl<M>

        fun byIndex(index: String): ByDsl<M>
    }

    fun <M : Any> find(type: KClass<M>, vararg options: Options.Read): FindDsl<M>

    fun getIndexesOf(key: Key<*>, vararg options: Options.Read): List<String>

}

inline operator fun <reified M : Any> DBRead.get(key: Key<M>, vararg options: Options.Read) = get(M::class, key, *options)
inline fun <reified M : Any> DBRead.find(vararg options: Options.Read) = find(M::class, *options)
