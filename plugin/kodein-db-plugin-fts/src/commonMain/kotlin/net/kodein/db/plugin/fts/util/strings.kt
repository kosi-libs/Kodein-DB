package net.kodein.db.plugin.fts.util

inline fun String.withoutSuffix(region: String, suffixes: Iterable<String>, block: (wordWithout: String) -> Unit) {
    val found = suffixes.firstOrNull { region.endsWith(it, ignoreCase = true) }
    if (found != null) block(this - found.length)
}

inline fun String.regionWithoutSuffix(region: String, suffixes: Iterable<String>, block: (regionWithout: String, wordWithout: String) -> Unit) {
    val found = suffixes.firstOrNull { region.endsWith(it, ignoreCase = true) }
    if (found != null) block(region - found.length, this - found.length)
}

fun String.endsWith(chars: CharArray): Boolean = lastOrNull()?.let { it in chars } ?: false

operator fun String.minus(suffixLength: Int) = substring(0, length - suffixLength)

fun String.prefixed(vararg prefixes: String): Boolean = prefixes.any { startsWith(it) }

fun String.indices(start: Int) = start.coerceAtMost(length) until length
