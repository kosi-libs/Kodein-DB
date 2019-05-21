package org.kodein.db.model

import org.kodein.db.Options

interface ModelDBFactory {
    fun open(path: String, vararg options: Options.Open): ModelDB
    fun destroy(path: String, vararg options: Options.Open)

    class Based(private val baseWithSeparator: String, private val factory: ModelDBFactory) : ModelDBFactory {
        override fun open(path: String, vararg options: Options.Open) = factory.open(baseWithSeparator + path, *options)
        override fun destroy(path: String, vararg options: Options.Open) = factory.destroy(baseWithSeparator + path, *options)
    }
}

fun ModelDBFactory.based(baseWithSeparator: String) = ModelDBFactory.Based(baseWithSeparator, this)
