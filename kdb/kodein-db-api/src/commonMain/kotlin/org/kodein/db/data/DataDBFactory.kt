package org.kodein.db.data

import org.kodein.db.Options

interface DataDBFactory {
    fun open(path: String, vararg options: Options.Open): DataDB
    fun destroy(path: String, vararg options: Options.Open)

    class Based(private val baseWithSeparator: String, private val factory: DataDBFactory) : DataDBFactory {
        override fun open(path: String, vararg options: Options.Open) = factory.open(baseWithSeparator + path, *options)
        override fun destroy(path: String, vararg options: Options.Open) = factory.destroy(baseWithSeparator + path, *options)
    }
}

fun DataDBFactory.based(baseWithSeparator: String) = DataDBFactory.Based(baseWithSeparator, this)
