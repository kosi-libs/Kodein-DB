package net.kodein.db.plugin.fts.impl

import net.kodein.db.plugin.fts.Stemmer

internal class RegionsImpl : Stemmer.Regions {
    override val R = HashMap<String, Int>()

    class Creator(val name: String, val finder: (String) -> Int?)
    private val creators = ArrayList<Creator>()

    override fun String.invoke(builder: (String) -> Int?) { creators.add(Creator(this, builder)) }

    internal fun findFor(word: String): Map<String, Int> {
        R.clear()
        creators.forEach { creator ->
            creator.finder(word)?.let { R[creator.name] = it }
        }
        creators.forEach { R.getOrPut(it.name) { word.length } }
        return R
    }
}