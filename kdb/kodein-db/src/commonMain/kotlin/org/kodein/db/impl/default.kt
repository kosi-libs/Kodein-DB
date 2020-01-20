package org.kodein.db.impl

import org.kodein.db.*

expect val DB.Companion.factory: DBFactory<DB>

fun DB.Companion.open(path: String, vararg options: Options.Open): DB = DB.factory.open(path, *options)
fun DB.Companion.destroy(path: String, vararg options: Options.Open) = DB.factory.destroy(path, *options)

fun DB.Companion.prefixed(prefix: String): DBFactory.Based<DB> = DB.factory.prefixed(prefix)
fun DB.Companion.inDir(path: String): DBFactory.Based<DB> = DB.factory.inDir(path)
