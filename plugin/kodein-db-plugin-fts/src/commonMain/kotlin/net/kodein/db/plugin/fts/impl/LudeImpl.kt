package net.kodein.db.plugin.fts.impl

import net.kodein.db.plugin.fts.Stemmer

class LudeImpl(private val stemmer: Stemmer) : Stemmer.Lude {

    class ConditionImpl : Stemmer.Lude.Condition {
        var word: String = ""
        var index: Int = 0

        override fun precededBy(vararg p: Char): Boolean = index > 0 && word[index - 1] in p
        override fun followedBy(vararg p: Char): Boolean = index < word.lastIndex && word[index + 1] in p
        override fun isFirst(): Boolean = index == 0
        override fun isLast(): Boolean = index == word.lastIndex
    }

    class Change(val chars: CharArray, val cond: Stemmer.Lude.Condition.() -> Boolean, val transf: (String, Int) -> Pair<String, Int>)
    private val changes = ArrayList<Change>()

    override fun toUppercase(vararg chars: Char, cond: Stemmer.Lude.Condition.() -> Boolean) {
        changes.add(Change(chars, cond, { token, index -> token[index].toUpperCase().toString() to 1 }))
    }

    override fun toLowercase(vararg chars: Char, cond: Stemmer.Lude.Condition.() -> Boolean) {
        changes.add(Change(chars, cond, { token, index -> token[index].toLowerCase().toString() to 1 }))
    }

    override fun replace(char: Char, with: String) {
        changes.add(Change(charArrayOf(char), { true }, { _, _ -> with to 1 }))
    }

    private val condition = ConditionImpl()
    private val stringBuilder = StringBuilder()

    private class CompactImpl(char: Char) : Stemmer.Lude.Compact {
        var default: String = char.toString()
        val map = HashMap<Char, String>()
        override fun and(char: Char, str: String) { map[char] = str }
        override fun default(str: String) { default = str }

    }

    override fun compact(char: Char, builder: Stemmer.Lude.Compact.() -> Unit) {
        val compact = CompactImpl(char).apply(builder)
        changes.add(Change(
                charArrayOf(char),
                { true },
                { token, index ->
                    if (index == token.lastIndex) compact.default to 1
                    else compact.map[token[index + 1]]?.let { it to 2 } ?: compact.default to 1
                }
        ))
    }

    internal fun execute(token: String): String = stringBuilder.apply {
        clear()
        condition.word = token
        var index = 0
        while (index < token.length) {
            condition.index = index
            val change = changes.firstOrNull { token[index] in it.chars && it.cond(condition) }
            if (change != null) {
                val (update, move) = change.transf(token, index)
                append(update)
                index += move
            }
            else {
                append(token[index])
                ++index
            }
        }
    }.toString()
}