package net.kodein.db.plugin.fts.lang

import net.kodein.db.plugin.fts.AccentsMap
import net.kodein.db.plugin.fts.Stemmer

interface Lang {
    val stemmer: Stemmer
    val stopWords: Set<String>
    val accentsMap: AccentsMap
}
