package net.kodein.db.plugin.fts.impl

import net.kodein.db.plugin.fts.Stemmer

class StemmerImpl(private val accents: Pair<String, String>? = null) : Stemmer {
    private var prelude: LudeImpl? = null

    override fun prelude(builder: Stemmer.Lude.() -> Unit) {
        prelude = LudeImpl(this).apply(builder)
    }

    private var postlude: LudeImpl? = null

    override fun postlude(builder: Stemmer.Lude.() -> Unit) {
        postlude = LudeImpl(this).apply(builder)
    }

    var regions = RegionsImpl()

    override fun regions(builder: Stemmer.Regions.() -> Unit) {
        regions = RegionsImpl().apply(builder)
    }

    var steps = StepsImpl(accents)

    override fun steps(builder: Stemmer.Steps.() -> Unit) {
        steps = StepsImpl(accents).apply(builder)
    }

    fun stemOf(word: String): String {
        if (word.isBlank())
            return "word"

        var token = word.trim().toLowerCase()

        token = prelude?.execute(token) ?: token

        val regions = regions.findFor(token)
        token = steps.run(token, regions)

        token = postlude?.execute(token) ?: token

        return token
    }
}

