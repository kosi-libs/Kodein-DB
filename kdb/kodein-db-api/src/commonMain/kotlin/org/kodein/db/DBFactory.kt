package org.kodein.db

public interface DBFactory<T : Any> {
    public fun open(path: String, vararg options: Options.Open): T
    public fun destroy(path: String, vararg options: Options.Open)

    public class Based<T : Any> internal constructor(private val baseWithSeparator: String, private val factory: DBFactory<T>) : DBFactory<T> {
        override fun open(path: String, vararg options: Options.Open): T = factory.open(baseWithSeparator + path, *options)
        override fun destroy(path: String, vararg options: Options.Open): Unit = factory.destroy(baseWithSeparator + path, *options)
    }
}

public fun <T : Any> DBFactory<T>.prefixed(prefix: String): DBFactory.Based<T> = DBFactory.Based(prefix, this)
public fun <T : Any> DBFactory<T>.inDir(path: String): DBFactory.Based<T> = prefixed("$path/")
