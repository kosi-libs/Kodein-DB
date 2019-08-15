package org.kodein.db.impl

import org.kodein.db.DB
import org.kodein.db.Middleware
import org.kodein.db.Options
import org.kodein.db.impl.model.cache.defaultCacheCopyMaxSize
import org.kodein.db.impl.model.cache.defaultCacheSize
import org.kodein.db.impl.model.cache.middleware
import org.kodein.db.invoke
import org.kodein.db.model.ModelDBFactory
import org.kodein.db.model.cache.ModelCache

internal abstract class AbstractDBFactory : DB.Factory {

   internal abstract fun mdbFactory(): ModelDBFactory

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
}
