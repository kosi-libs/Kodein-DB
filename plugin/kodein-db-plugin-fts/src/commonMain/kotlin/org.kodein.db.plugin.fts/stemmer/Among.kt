package org.kodein.db.plugin.fts.stemmer

internal class Among(
    str: String, /* search string */
    val substring_i: Int, /* index to longest matching substring */
    val result: Int, /* result of the lookup */
    val process: (SnowballProgram.() -> Boolean)? = null
) {
    val s = str.toCharArray()
	val s_size: Int = str.length /* search string */
}

@Suppress("UNCHECKED_CAST")
internal fun <T : SnowballProgram> Among(
    str: String,
    substring_i: Int,
    result: Int,
    process: (T.() -> Boolean)? = null
) = Among(str, substring_i, result, process as (SnowballProgram.() -> Boolean))
