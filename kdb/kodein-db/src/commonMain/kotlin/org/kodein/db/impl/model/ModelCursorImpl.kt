package org.kodein.db.impl.model

import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.db.data.DataCursor
import org.kodein.db.model.*
import org.kodein.memory.cache.Sized
import org.kodein.memory.io.ReadBuffer
import kotlin.reflect.KClass

internal class ModelCursorImpl<B : Any, M : B>(private val dc: DataCursor, private val typeTable: TypeTable, private val serializer: Serializer, private val modelType: KClass<M>) : ModelCursor<M> {

    override fun isValid(): Boolean = dc.isValid()

    override fun next() = dc.next()

    override fun prev() = dc.prev()

    override fun seekToFirst() = dc.seekToFirst()

    override fun seekToLast() = dc.seekToLast()

    override fun seekTo(target: ReadBuffer) = dc.seekTo(target)

    override fun transientKey() = TransientKey(Key.Heap(modelType, dc.transientKey()))

    override fun model(vararg options: Options.Read): Sized<M> = BaseModelRead.getFrom(dc.transientValue(), modelType, typeTable, serializer, options)

    override fun transientSeekKey() = TransientSeekKey(dc.transientSeekKey())

    private inner class Entries(private val dce: DataCursor.Entries) : ModelCursor.Entries<M> {

        override val size: Int get() = dce.size

        override fun getSeekKey(i: Int): ReadBuffer = dce.getSeekKey(i)

        override fun getKey(i: Int) = Key.Heap(modelType, dce.getKey(i))

        override fun getModel(i: Int, vararg options: Options.Read): Sized<M> = BaseModelRead.getFrom(dce.getValue(i), modelType, typeTable, serializer, options)

        override fun close() = dce.close()
    }

    override fun nextEntries(size: Int): ModelCursor.Entries<M> = Entries(dc.nextEntries(size))

    override fun close() = dc.close()
}
