package org.kodein.db

interface DBFactory<T : Any> {
    fun open(path: String, vararg options: Options.Open): T
    fun destroy(path: String, vararg options: Options.Open)

    class Based<T : Any> internal constructor(private val baseWithSeparator: String, private val factory: DBFactory<T>) : DBFactory<T> {
        override fun open(path: String, vararg options: Options.Open) = factory.open(baseWithSeparator + path, *options)
        override fun destroy(path: String, vararg options: Options.Open) = factory.destroy(baseWithSeparator + path, *options)
    }
}

fun <T : Any> DBFactory<T>.prefixed(prefix: String) = DBFactory.Based(prefix, this)
fun <T : Any> DBFactory<T>.inDir(path: String) = prefixed("$path/")
