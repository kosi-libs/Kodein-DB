package org.kodein.db.impl

import org.kodein.db.*

expect val DB.Companion.default: DBFactory<DB>

fun DB.Companion.open(path: String, vararg options: Options.Open): DB = DB.default.open(path, *options)
fun DB.Companion.destroy(path: String, vararg options: Options.Open) = DB.default.destroy(path, *options)

fun DB.Companion.prefixed(prefix: String): DBFactory.Based<DB> = DB.default.prefixed(prefix)
fun DB.Companion.inDir(path: String): DBFactory.Based<DB> = DB.default.inDir(path)
