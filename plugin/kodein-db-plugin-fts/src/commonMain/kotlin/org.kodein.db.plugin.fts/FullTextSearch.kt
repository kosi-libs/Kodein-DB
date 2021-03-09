package org.kodein.db.plugin.fts

import org.kodein.db.Middleware
import org.kodein.db.Options


public val FullTextSearch: Options.Open = Middleware.Model { FtsModelDB(it) }
