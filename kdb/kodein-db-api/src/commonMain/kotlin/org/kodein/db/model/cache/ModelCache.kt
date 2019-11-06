package org.kodein.db.model.cache

import org.kodein.db.Options
import org.kodein.db.Sized

interface ModelCache : BaseModelCache {

    object Disable : Options.Open
    data class MaxSize(val maxSize: Long) : Options.Open
    data class CopyMaxSize(val maxSize: Long) : Options.Open, Options.Read
    object Skip : Options.Read, Options.Write
    object Refresh : Options.Read

    sealed class Entry<M : Any> {
        abstract val model: M?
        abstract val size: Int

        data class Cached<M : Any>(override val model: M, override val size: Int) : Entry<M>(), Sized<M>
        object Deleted : Entry<Nothing>() { override val model = null ; override val size: Int = 8 }
        object NotInCache : Entry<Nothing>() { override val model = null ; override val size: Int = 0 }
    }

    val entryCount: Int
    val size: Long
    val maxSize: Long

    val hitCount: Int
    val missCount: Int
    val retrieveCount: Int
    val putCount: Int
    val deleteCount: Int
    val evictionCount: Int

    fun newCopy(copyMaxSize: Long): ModelCache

    companion object

}
