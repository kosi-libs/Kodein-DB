package org.kodein.db.impl

import org.kodein.db.*

public expect val DB.Companion.default: DBFactory<DB>

public fun DB.Companion.open(path: String, vararg options: Options.Open): DB = DB.default.open(path, *options)
public fun DB.Companion.destroy(path: String, vararg options: Options.Open): Unit = DB.default.destroy(path, *options)

public fun DB.Companion.prefixed(prefix: String): DBFactory.Based<DB> = DB.default.prefixed(prefix)
public fun DB.Companion.inDir(path: String): DBFactory.Based<DB> = DB.default.inDir(path)
