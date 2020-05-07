package net.kodein.db.plugin.fts

import net.kodein.db.plugin.fts.impl.StemmerImpl

@DslMarker
@Target(AnnotationTarget.CLASS)
annotation class StemmerDSL


@StemmerDSL
interface Stemmer {
    fun prelude(builder: Lude.() -> Unit)
    fun regions(builder: Regions.() -> Unit)
    fun steps(builder: Steps.() -> Unit)
    fun postlude(builder: Lude.() -> Unit)

    @StemmerDSL
    interface Lude {
        interface Condition {
            fun precededBy(vararg p: Char): Boolean
            fun followedBy(vararg p: Char): Boolean
            fun isFirst(): Boolean
            fun isLast(): Boolean
        }

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
    interface Regions {
        val V get() = 0
        val R: Map<Int, Int>

        operator fun Int.invoke(builder: (String) -> Int?)
    }

    @StemmerDSL
    interface Steps {
        var firstStep: Int
        operator fun Int.invoke(builder: Step.() -> Step.NextStep): Int
    }

    @StemmerDSL
    interface Step {
        val end: Int get() = Int.MAX_VALUE

        interface Exec {
            val V: Int get() = 0
            val R: Map<Int, Int>
            val token: String
        }

        interface SuffixExec : Exec {
            fun delete(chars: Int = Int.MAX_VALUE, nextStep: Int = -1)
            fun replaceWith(str: String, nextStep: Int = -1)
            fun precededBy(vararg suffixPrefixes: String, action: Exec.(Int) -> Unit)
        }

        fun suffix(vararg suffixes: String, action: SuffixExec.(Int) -> Unit)
        fun findSuffix(find: (String) -> Int, action: SuffixExec.(Int) -> Unit)

        data class NextStep(val noChange: Int, val changed: Int)
        fun nextStep(noChange: Int, changed: Int) = NextStep(noChange, changed)
        fun nextStep(next: Int) = NextStep(next, next)
    }
}

fun stemmer(builder: Stemmer.() -> Unit): (String) -> String {
    val stemmer = StemmerImpl().apply(builder)
    return { stemmer.stemOf(it) }
}

fun unAccentedStemmer(accents: Pair<String, String>, builder: Stemmer.() -> Unit): (String) -> String {
    val stemmer = StemmerImpl(accents).apply(builder)
    return { stemmer.stemOf(it) }
}

data class Stem(val token: Token, val stem: String)

fun Sequence<Token>.toStems(stemmer: (String) -> String): Sequence<Stem> = map { Stem(it, stemmer(it.word)) }
