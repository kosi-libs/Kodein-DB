package net.kodein.db.plugin.fts.impl

import net.kodein.db.plugin.fts.AccentsMap
import net.kodein.db.plugin.fts.Stemmer

internal class StemmerImpl(accentsMap: AccentsMap?, builder: Stemmer.Algorithm.() -> Unit) : Stemmer {

    private val algorithm = AlgorithmImpl(accentsMap).apply(builder)

    override fun stemOf(word: String): String {
        if (word.isBlank())
            return word

        val token = word.trim().toLowerCase()

        return algorithm.execute(token, emptyMap()).token
    }
}

