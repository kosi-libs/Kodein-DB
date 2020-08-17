package org.kodein.db.impl

import org.kodein.db.*
import org.kodein.db.impl.model.cache.defaultCacheCopyMaxSize
import org.kodein.db.impl.model.cache.defaultCacheSize
import org.kodein.db.impl.model.cache.middleware
import org.kodein.db.model.ModelDB
import org.kodein.db.model.cache.ModelCache

public abstract class AbstractDBFactory : DBFactory<DB> {

   internal abstract fun mdbFactory(): DBFactory<ModelDB>

    override fun open(path: String, vararg options: Options.Open): DB {
        val mdbOptions = if (options<ModelCache.Disable>() == null) {
            val middleware = ModelCache.middleware(
                    maxSize = options<ModelCache.MaxSize>()?.maxSize ?: defaultCacheSize,
                    copyMaxSize =  options<ModelCache.CopyMaxSize>()?.maxSize ?: defaultCacheCopyMaxSize()
            )
            arrayOf<Options.Open>(Middleware.Model(middleware)) + options
        } else {
            options
        }

        val mdb = mdbFactory().open(path, *mdbOptions)

        return DBImpl(mdb)
    }

    override fun destroy(path: String, vararg options: Options.Open) {
        mdbFactory().destroy(path, *options)
    }
}
