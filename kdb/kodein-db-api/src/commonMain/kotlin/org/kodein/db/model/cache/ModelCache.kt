package org.kodein.db.model.cache

import org.kodein.db.Options
import org.kodein.db.Sized

public interface ModelCache : BaseModelCache {

    public object Disable : Options.Open
    public data class MaxSize(val maxSize: Long) : Options.Open
    public data class CopyMaxSize(val maxSize: Long) : Options.Open, Options.Read
    public object Skip : Options.Read, Options.Write
    public object Refresh : Options.Read

    public sealed class Entry<M : Any> {
        public abstract val model: M?
        public abstract val size: Int

        public data class Cached<M : Any>(override val model: M, override val size: Int) : Entry<M>(), Sized<M>
        public object Deleted : Entry<Nothing>() { override val model: Nothing? = null ; override val size: Int = 8 }
        public object NotInCache : Entry<Nothing>() { override val model: Nothing? = null ; override val size: Int = 0 }
    }

    public val entryCount: Int
    public val size: Long
    public val maxSize: Long

    public val hitCount: Int
    public val missCount: Int
    public val retrieveCount: Int
    public val putCount: Int
    public val deleteCount: Int
    public val evictionCount: Int

    public fun newCopy(copyMaxSize: Long): ModelCache

    public companion object

}
