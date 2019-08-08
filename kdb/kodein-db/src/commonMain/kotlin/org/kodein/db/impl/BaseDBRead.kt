package org.kodein.db.impl

import org.kodein.db.*
import org.kodein.db.model.ModelRead
import kotlin.reflect.KClass

internal interface BaseDBRead : DBRead, BaseDBBase {

    override val mdb: ModelRead

    @Suppress("ReplaceGetOrSet")
    override fun <M : Any> get(key: Key<M>, vararg options: Options.Read): M? = mdb.get(key, *options)?.value

    override fun findAll(vararg options: Options.Read): DBCursor<*> = DBCursorImpl(mdb.findAll(*options))

    override fun <M : Any> find(type: KClass<M>, vararg options: Options.Read): DBRead.FindDsl<M> = FindDslImpl(mdb, type, options)

    override fun getIndexesOf(key: Key<*>, vararg options: Options.Read): List<String> = mdb.getIndexesOf(key, *options)

    class FindDslImpl<M : Any>(private val mdb: ModelRead, private val type: KClass<M>, private val options: Array<out Options.Read>) : DBRead.FindDsl<M> {

        override fun byPrimaryKey(): DBRead.FindDsl.ByDsl<M> = object : DBRead.FindDsl.ByDsl<M> {
            override fun all(): DBCursor<M> = DBCursorImpl(mdb.findAllByType(type, *options))
            override fun withValue(value: Value, isOpen: Boolean): DBCursor<M> = DBCursorImpl(mdb.findByPrimaryKey(type, value, isOpen, *options))
        }

        override fun byIndex(name: String): DBRead.FindDsl.ByDsl<M> = object : DBRead.FindDsl.ByDsl<M> {
            override fun all(): DBCursor<M> = DBCursorImpl(mdb.findAllByIndex(type, name, *options))
            override fun withValue(value: Value, isOpen: Boolean): DBCursor<M> = DBCursorImpl(mdb.findByIndex(type, name, value, isOpen, *options))
        }

    }

}
