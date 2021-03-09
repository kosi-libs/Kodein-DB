package org.kodein.db.plugin.fts

import org.kodein.memory.io.ExpandableBuffer
import org.kodein.memory.io.ReadBuffer
import org.kodein.memory.io.Readable


public fun CharSequence.tokens(): Sequence<Pair<String, Int>> =
    sequence {
        var start = -1
        for (pos in indices) {
            when {
                start == -1 && !(get(pos).isWhitespace()) -> {
                    start = pos
                }
                start != -1 && get(pos).isWhitespace() -> {
                    yield(substring(start, pos) to start)
                    start = -1
                }
            }
        }
        if (start != -1) yield(substring(start) to start)
    }


public fun Sequence<Pair<String, Int>>.asFts(lang: Lang): FtsTokens {
    val tokens = HashMap<String, ExpandableBuffer>()
    val stemmer = lang.stemmer()
    val stopWords = lang.stopwords()

    forEach { (token, position) ->
        if (token !in stopWords) {
            val stem = stemmer.stemOf(token)
            tokens.getOrPut(stem) { ExpandableBuffer.array(4) } .putInt(position)
        }
    }

    tokens.forEach { (_, buffer) -> buffer.flip() }
    return tokens
}

public fun CharSequence.ftsTokens(lang: Lang): FtsTokens = tokens().asFts(lang)

public fun Sequence<Pair<String, Int>>.asFts(): FtsTokens {
    val tokens = HashMap<String, ExpandableBuffer>()
    forEach { (token, position) ->
        tokens.getOrPut(token) { ExpandableBuffer.array(4) } .putInt(position)
    }
    tokens.forEach { (_, buffer) -> buffer.flip() }
    return tokens
}

public fun CharSequence.ftsTokens(): FtsTokens = tokens().asFts()
