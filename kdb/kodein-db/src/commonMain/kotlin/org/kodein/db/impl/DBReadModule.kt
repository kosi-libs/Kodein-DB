package org.kodein.db.impl

import org.kodein.db.*
import org.kodein.db.model.ModelRead
import kotlin.reflect.KClass

internal interface DBReadModule : DBRead {

    val mdb: ModelRead

    @Suppress("ReplaceGetOrSet")
    override fun <M : Any> get(type: KClass<M>, key: Key<M>, vararg options: Options.Read): M? = mdb.get(type, key, *options)?.model

    override fun findAll(vararg options: Options.Read): Cursor<*> = CursorImpl(mdb.findAll(*options))

    override fun <M : Any> find(type: KClass<M>, vararg options: Options.Read): DBRead.FindDsl<M> = FindDslImpl(mdb, type, options)

    override fun getIndexesOf(key: Key<*>, vararg options: Options.Read): List<String> = mdb.getIndexesOf(key, *options)

    class FindDslImpl<M : Any>(private val mdb: ModelRead, private val type: KClass<M>, private val options: Array<out Options.Read>) : DBRead.FindDsl<M> {

        override fun byId(): DBRead.FindDsl.ByDsl<M> = object : DBRead.FindDsl.ByDsl<M> {
            override fun all(): Cursor<M> = CursorImpl(mdb.findAllByType(type, *options))
            override fun withValue(value: Value, isOpen: Boolean): Cursor<M> = CursorImpl(mdb.findById(type, value, isOpen, *options))
        }

        override fun byIndex(index: String): DBRead.FindDsl.ByDsl<M> = object : DBRead.FindDsl.ByDsl<M> {
            override fun all(): Cursor<M> = CursorImpl(mdb.findAllByIndex(type, index, *options))
            override fun withValue(value: Value, isOpen: Boolean): Cursor<M> = CursorImpl(mdb.findByIndex(type, index, value, isOpen, *options))
        }

    }

}
