package net.kodein.db.plugin.fts.impl

import net.kodein.db.plugin.fts.Stemmer

class RegionsImpl : Stemmer.Regions {
    override val R = HashMap<Int, Int>()

    class Creator(val num: Int, val finder: (String) -> Int?)
    private val creators = ArrayList<Creator>()

    override fun Int.invoke(builder: (String) -> Int?): Unit { creators.add(Creator(this, builder)) }

    internal fun findFor(word: String): Map<Int, Int> {
        R.clear()
        creators.forEach { creator ->
            creator.finder(word)?.let { R.put(creator.num, it) }
        }
        creators.forEach { R.getOrPut(it.num) { word.length } }
        return R
    }
}