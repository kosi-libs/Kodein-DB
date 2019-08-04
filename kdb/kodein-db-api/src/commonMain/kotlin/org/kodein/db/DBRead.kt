@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package org.kodein.db

import org.kodein.db.model.ModelCursor

interface DBRead : DBBase {

    operator fun <M : Any> get(key: Key<M>, vararg options: Options.Read): M?

    fun findAll(vararg options: Options.Read): ModelCursor<*>

    interface FindDsl<M : Any> {

        interface ByDsl<M : Any> {

            fun all(): ModelCursor<M>

            fun withValue(value: Value, isOpen: Boolean = true): ModelCursor<M>
        }

        fun all(): ModelCursor<M> = byPrimaryKey().all()

        fun byPrimaryKey(): ByDsl<M>

        fun byIndex(name: String): ByDsl<M>
    }

    fun <M : Any> find(vararg options: Options.Read)

    fun getIndexesOf(key: Key<*>, vararg options: Options.Read): List<String>

}
