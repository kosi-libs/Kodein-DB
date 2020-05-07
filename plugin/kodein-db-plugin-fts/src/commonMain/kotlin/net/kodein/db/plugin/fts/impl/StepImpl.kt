package net.kodein.db.plugin.fts.impl

import net.kodein.db.plugin.fts.Stemmer
import net.kodein.db.plugin.fts.unaccented

class StepImpl(val accents: Pair<String, String>?) : Stemmer.Step {

    lateinit var nextStep: Stemmer.Step.NextStep

    class Entry(val test: (String) -> Int, val action: (Int) -> Unit)
    private val entries = ArrayList<Entry>()

    override fun suffix(vararg suffixes: String, action: Stemmer.Step.SuffixExec.(Int) -> Unit) {
        val search = (
                if (accents != null) suffixes.toSet() + (suffixes.map { it.unaccented(accents) } .toSet())
                else suffixes.toSet()
            ).sortedByDescending { it.length }
        entries.add(Entry(
                { token ->
                    search.firstOrNull { token.endsWith(it) }
                            ?.let { token.length - it.length }
                            ?: -1
                },
                { suffixExec.action(it) }
        ))
    }

    override fun findSuffix(find: (String) -> Int, action: Stemmer.Step.SuffixExec.(Int) -> Unit) {
        entries.add(Entry(
                find,
                { suffixExec.action(it) }
        ))
    }

    private var lastAction: ((String) -> Pair<String, Int>)? = null

    private inner class SuffixExecImpl : Stemmer.Step.SuffixExec {
        override lateinit var R: Map<Int, Int>
        override lateinit var token: String
        var suffixStart: Int = -1

        override fun delete(chars: Int, nextStep: Int) {
            val suffixStart = suffixStart // capture!
            lastAction = {
                val count = chars.coerceAtMost(it.length - suffixStart)
                it.substring(0, it.length - count) to nextStep
            }
        }
        override fun replaceWith(str: String, nextStep: Int) {
            val suffixStart = suffixStart // capture!
            lastAction = {
                (it.substring(0, suffixStart) + str) to nextStep
            }
        }
        override fun precededBy(vararg suffixPrefixes: String, action: Stemmer.Step.Exec.(Int) -> Unit) {
            val tokenWithoutSuffix = token.substring(0, suffixStart)
            val found = suffixPrefixes.firstOrNull { tokenWithoutSuffix.endsWith(it) }
                    ?.let { tokenWithoutSuffix.length - it.length }
                    ?: -1
            if (found >= 0) {
                val previousStart = suffixStart
                suffixStart = found
                action(found)
                suffixStart = previousStart
            }
        }
    }

    private val suffixExec = SuffixExecImpl()

    @Suppress("NAME_SHADOWING")
    fun execute(token: String, regions: Map<Int, Int>): Pair<String, Int> {
        suffixExec.R = regions
        suffixExec.token = token
        for (entry in entries) {
            val p = entry.test(token)
            if (p >= 0) {
                lastAction = null
                suffixExec.suffixStart = p
                entry.action(p)
                lastAction?.let {
                    val pair = it.invoke(token)
                    if (pair.second == -1) return pair.first to nextStep.changed
                    else return pair
                }
            }
        }
        return token to nextStep.noChange
    }

}
