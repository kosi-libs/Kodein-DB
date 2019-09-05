@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package org.kodein.db

import kotlin.reflect.KClass

interface DBRead : KeyMaker {

    operator fun <M : Any> get(key: Key<M>, vararg options: Options.Read): M?

    fun findAll(vararg options: Options.Read): DBCursor<*>

    interface FindDsl<M : Any> {

        interface ByDsl<M : Any> {

            fun all(): DBCursor<M>

            fun withValue(value: Value, isOpen: Boolean = true): DBCursor<M>
        }

        fun all(): DBCursor<M> = byPrimaryKey().all()

        fun byPrimaryKey(): ByDsl<M>

        fun byIndex(name: String): ByDsl<M>
    }

    fun <M : Any> find(type: KClass<M>, vararg options: Options.Read): FindDsl<M>

    fun getIndexesOf(key: Key<*>, vararg options: Options.Read): List<String>

}

inline fun <reified M : Any> DBRead.find(vararg options: Options.Read) = find(M::class, *options)
