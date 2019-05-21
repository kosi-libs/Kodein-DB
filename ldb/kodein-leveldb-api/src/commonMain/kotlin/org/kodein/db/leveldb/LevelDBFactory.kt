package org.kodein.db.leveldb

/**
 * A Sober-LevelDB factory is responsible for opening or destroying LevelDB databases.
 */
interface LevelDBFactory {
    /**
     * Open or create a LevelDB database.
     *
     * The cache will be LevelDB default 8MB cache.
     *
     * @param path The path to the database to open.
     * @param options The database options.
     * @return The LevelDB Java object.
     */
    fun open(path: String, options: LevelDB.Options = LevelDB.Options.DEFAULT): LevelDB

    /**
     * Destroy a LevelDB database, if it exists.
     *
     * @param path The path to the database to destroy.
     */
    fun destroy(path: String, options: LevelDB.Options = LevelDB.Options.DEFAULT)

    // TODO: Add these methods
//        fun dumpFile(??)
//
//        fun buildTable(file: String, append: Boolean = true, options: Options = Options.DEFAULT)
//        fun openTable(file: String, size: Int = -1, options: Options = Options.DEFAULT): Table


    class Based(private val baseWithSeparator: String, private val factory: LevelDBFactory) : LevelDBFactory {
        override fun open(path: String, options: LevelDB.Options) = factory.open(baseWithSeparator + path, options)
        override fun destroy(path: String, options: LevelDB.Options) = factory.destroy(baseWithSeparator + path, options)
    }
}

fun LevelDBFactory.based(baseWithSeparator: String) = LevelDBFactory.Based(baseWithSeparator, this)
