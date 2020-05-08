package net.kodein.db.plugin.fts.impl

import net.kodein.db.plugin.fts.AccentsMap
import net.kodein.db.plugin.fts.RegionsMap
import net.kodein.db.plugin.fts.Stemmer
import net.kodein.db.plugin.fts.unaccented
import kotlin.math.absoluteValue

internal class SearchesImpl(val accents: AccentsMap?) : Stemmer.Searches, StepImpl {

    class InSuffixImpl : Stemmer.Searches.InSuffix {

        var exec: (Stemmer.Searches.Exec.(Int) -> Unit)? = null
        val prefixes = ArrayList<Pair<List<String>, InSuffixImpl>>()

        override fun exec(action: Stemmer.Searches.Exec.(Int) -> Unit) {
            check(exec == null) { "Exec has already been defined for this prefix" }
            exec = action
        }

        override fun precededBy(vararg prefixes: String, action: Stemmer.Searches.InSuffix.() -> Unit) {
            val search = prefixes.sortedByDescending { it.length }
            this.prefixes.add(search to InSuffixImpl().apply(action))
        }

        internal fun execute(exec: ExecImpl, suffixStart: Int) {
            if (prefixes.isNotEmpty()) {
                val tokenWithoutSuffix = exec.token.substring(0, suffixStart)
                prefixes.forEach { (prefixList, inSuffix) ->
                    val found = prefixList.firstOrNull { tokenWithoutSuffix.endsWith(it) }
                            ?.let {
                                tokenWithoutSuffix.length - it.length
                            }
                            ?: -1
                    if (found >= 0) {
                        inSuffix.execute(exec, found)
                        if (exec.action != null) return
                    }
                }
            }
            this.exec?.invoke(exec, suffixStart)
        }
    }

    val entries = ArrayList<Pair<Stemmer.HasRegions.(String) -> Int, InSuffixImpl>>()

    override fun suffix(vararg suffixes: String, builder: Stemmer.Searches.InSuffix.() -> Unit) {
        val search = (
                if (accents != null) suffixes.toSet() + (suffixes.map { it.unaccented(accents) } .toSet())
                else suffixes.toSet()
            ).sortedByDescending { it.length }
        val find: Stemmer.HasRegions.(String) -> Int = { token ->
            search.firstOrNull { token.endsWith(it) }
                    ?.let { token.length - it.length }
                    ?: -1
        }
        entries.add(find to InSuffixImpl().apply(builder))
    }

    override fun find(find: Stemmer.HasRegions.(String) -> Int, builder: Stemmer.Searches.InSuffix.() -> Unit) {
        entries.add(find to InSuffixImpl().apply(builder))
    }

    internal class ExecImpl : Stemmer.Searches.Exec {
        var action: ((String) -> StepImpl.Return)? = null
        override lateinit var R: Map<String, Int>
        override lateinit var token: String

        override fun delete(suffixStart: Int, nextStep: String?) {
            action = {
                val end = if (suffixStart < 0 ) (it.length - suffixStart.absoluteValue).coerceAtLeast(0) else suffixStart.coerceAtMost(it.length)
                StepImpl.Return(it.substring(0, end), nextStep)
            }
        }

        override fun replace(suffixStart: Int, with: String, nextStep: String?) {
            action = {
                StepImpl.Return(it.substring(0, suffixStart) + with, nextStep)
            }
        }

        override fun add(suffix: String, nextStep: String?) {
            action = {
                StepImpl.Return(it + suffix, nextStep)
            }
        }

        override fun nop(nextStep: String?) {
            action = { StepImpl.Return(it, nextStep) }
        }
    }

    private val exec = ExecImpl()

    override fun execute(token: String, regions: RegionsMap): StepImpl.Return {
        exec.R = regions
        exec.token = token
        exec.action = null

        entries.forEach { (test, inSuffix) ->
            val found = test(exec, token)
            if (found >= 0) {
                inSuffix.execute(exec, found)
                exec.action?.let { action ->
                    return action(token)
                }
            }
        }
        return StepImpl.Return(token, "")
    }
}
