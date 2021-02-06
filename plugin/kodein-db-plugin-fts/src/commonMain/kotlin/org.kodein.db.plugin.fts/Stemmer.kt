package org.kodein.db.plugin.fts


public fun interface Stemmer {

    public fun stemOf(word: String): String

}
