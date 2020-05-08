package net.kodein.db.plugin.fts

import net.kodein.db.plugin.fts.impl.StemmerImpl

@DslMarker
@Target(AnnotationTarget.CLASS)
annotation class StemmerDSL


typealias AccentsMap = Pair<String, String>
typealias RegionsMap = Map<String, Int>

@StemmerDSL
interface Stemmer {

    interface HasRegions {
        val R: Map<String, Int>
    }

    @StemmerDSL
    interface Regions : HasRegions {
        operator fun String.invoke(builder: (String) -> Int?)
    }

    @StemmerDSL
    interface Step {
        fun go(noChange: String?, changed: String?) = Algorithm.Go(noChange, changed)
        fun go(to: String?) = Algorithm.Go(to, to)
        fun done() = Algorithm.Go(null, null)
    }

    @StemmerDSL
    interface Changes : Step {
        @StemmerDSL
        interface Condition {
            fun precededBy(vararg p: Char): Boolean
            fun followedBy(vararg p: Char): Boolean
            fun isFirst(): Boolean
            fun isLast(): Boolean
        }

        @StemmerDSL
        interface Compact {
            fun and(char: Char, str: String)
            fun default(str: String)
        }

        fun toUppercase(vararg chars: Char, cond: Condition.() -> Boolean = { true })
        fun toLowercase(vararg chars: Char, cond: Condition.() -> Boolean = { true })
        fun replace(char: Char, with: String)
        fun compact(char: Char, builder: Compact.() -> Unit)
    }

    @StemmerDSL
    interface Searches : Step {
        @StemmerDSL
        interface Exec : HasRegions {
            val token: String

            fun delete(suffixStart: Int, nextStep: String? = "")
            fun replace(suffixStart: Int, with: String, nextStep: String? = "")
            fun add(suffix: String, nextStep: String? = "")
            fun nop(nextStep: String? = "")

            operator fun Int.contains(p: Int): Boolean = this <= p
        }

        @StemmerDSL
        interface InSuffix {
            fun precededBy(vararg prefixes: String, action: InSuffix.() -> Unit)
            fun exec(action: Exec.(Int) -> Unit)
        }

        fun suffix(vararg suffixes: String, builder: InSuffix.() -> Unit)

        fun find(find: HasRegions.(String) -> Int, builder: InSuffix.() -> Unit)
    }

    @StemmerDSL
    interface Transforms : Step {
        @StemmerDSL
        interface Exec : HasRegions {
            fun to(token: String, nextStep: String? = "")
        }

        fun exec(transform: Exec.(String) -> Unit)
    }

    @StemmerDSL
    interface Algorithm : Step {
        data class Go(val noChange: String?, val changed: String?)

        var firstStep: String

        fun regions(builder: Regions.() -> Unit)

        infix fun String.changes(builder: Changes.() -> Go): String
        infix fun String.executes(builder: Algorithm.() -> Go): String
        infix fun String.searches(builder: Searches.() -> Go): String
        infix fun String.transforms(builder: Transforms.() -> Go): String
    }

    fun stemOf(word: String): String
}

fun stemmer(builder: Stemmer.Algorithm.() -> Unit): Stemmer =
        StemmerImpl(null, builder)

fun unAccentedStemmer(accents: AccentsMap, builder: Stemmer.Algorithm.() -> Unit): Stemmer =
        StemmerImpl(accents, builder)

data class Stem(val token: Token, val stem: String)

fun Sequence<Token>.toStems(stemmer: Stemmer): Sequence<Stem> = map { Stem(it, stemmer.stemOf(it.word)) }
