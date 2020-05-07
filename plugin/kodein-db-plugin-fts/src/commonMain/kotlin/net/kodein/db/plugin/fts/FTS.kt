package net.kodein.db.plugin.fts

import net.kodein.db.plugin.fts.lang.frStopWords
import kotlin.jvm.JvmName

enum class CharType { Delimiter, Joiner, Normal }

@Suppress("NOTHING_TO_INLINE")
private inline fun Char.type(): CharType {
    val i = toInt()
    return when  {
        /* 0000..0019 */ i <= 0x0019 -> CharType.Delimiter
        /* 001A       */ i == 0x001A -> CharType.Normal
        /* 001B..002F */ i <= 0x002F -> CharType.Delimiter
        /* 0030..0039 */ i <= 0x0039 -> CharType.Normal
        /* 003A..003F */ i <= 0x003F -> CharType.Delimiter
        /* 0040..005A */ i <= 0x005A -> CharType.Normal
        /* 005B..0060 */ i <= 0x0060 -> CharType.Delimiter
        /* 0061..007A */ i <= 0x007A -> CharType.Normal
        /* 007B..0080 */ i <= 0x0080 -> CharType.Delimiter
        /* 0081..0084 */ i <= 0x0084 -> CharType.Joiner
        /* 0085       */ i == 0x0085 -> CharType.Delimiter
        /* 0086..0087 */ i <= 0x0087 -> CharType.Joiner
        /* 0088..0095 */ i <= 0x0095 -> CharType.Delimiter
        /* 0096..0097 */ i <= 0x0097 -> CharType.Joiner
        /* 0098..00BF */ i <= 0x00BF -> CharType.Delimiter
        /* 00C0..00D6 */ i <= 0x00D6 -> CharType.Normal
        /* 00D7       */ i == 0x00D7 -> CharType.Delimiter
        /* 00D8..00F6 */ i <= 0x00F6 -> CharType.Normal
        /* 00F7       */ i == 0x00F7 -> CharType.Delimiter
        /* 0100..1FFF */ i <= 0x1FFF -> CharType.Normal
        /* 2000..2109 */ i <= 0x2109 -> CharType.Delimiter

        else -> CharType.Normal
    }
}

data class Token(val word: String, val position: Int)

fun Sequence<Char>.toTokens(): Sequence<Token> = sequence {
    val it = iterator()
    var pos = 0
    var start = 0
    val word = StringBuilder()
    while (true) {
        if (!it.hasNext()) {
            yield(Token(word.toString(), start))
            break
        }
        val c = it.next()
        ++pos
        when (c.type()) {
            CharType.Delimiter -> {
                yield(Token(word.toString(), start))
                word.clear()
                start = pos
            }
            CharType.Joiner -> {}
            CharType.Normal -> {
                word.append(c)
            }
        }
    }
}

fun String.unaccented(accents: Pair<String, String>) = buildString {
    this@unaccented.forEach { c ->
        accents.first.indexOf(c).takeIf { it != -1 } ?. let { append(accents.second[it]) } ?: append(c)
    }
}

fun Sequence<String>.minLength(minLength: Int) = filter { it.length >= minLength }
@JvmName("tokenMinLength")
fun Sequence<Token>.minLength(minLength: Int) = filter { it.word.length >= minLength }

fun Sequence<String>.filter(stopWords: Set<String>) = filter { it !in stopWords }
@JvmName("tokenFilter")
fun Sequence<Token>.filter(stopWords: Set<String>) = filter { it.word !in stopWords }

fun Sequence<String>.unaccented(accents: Pair<String, String>): Sequence<String> =
        map { it.unaccented(accents) }
@JvmName("tokenUnaccented")
fun Sequence<Token>.unaccented(accents: Pair<String, String>): Sequence<Token> =
        map { it.copy(word = it.word.unaccented(accents)) }
