package net.kodein.db.plugin.fts.lang

import net.kodein.db.plugin.fts.Stemmer
import net.kodein.db.plugin.fts.stemmer
import net.kodein.db.plugin.fts.util.indices


object EN : Lang {
    private val Stemmer.HasRegions.R1 get() = R.getValue("1")
    private val Stemmer.HasRegions.R2 get() = R.getValue("2")

    override val stemmer = stemmer {

        val vowels = charArrayOf('a', 'e', 'i', 'o', 'u', 'y')

        val doubles = arrayOf("bb", "dd", "ff", "gg", "mm", "nn", "pp", "rr", "tt")

        fun String.endsWithShortSyllable(): Boolean =
                length >= 2 && this[length - 1] !in vowels && this[length - 2] in vowels && (length == 2 || this[length - 3] !in vowels)

        firstStep = "exceptions" transforms {
            exec {
                when (it) {
                    "skis" -> to("ski")
                    "skies" -> to("sky")
                    "dying" -> to("die")
                    "lying" -> to("lie")
                    "tying" -> to("tie")
                    "idly" -> to("idl")
                    "gently" -> to("gentl")
                    "ugly" -> to("ugli")
                    "only" -> to("onli")
                    "singly" -> to("singl")
                }
            }
            go(noChange = "invariants", changed = null)
        }

        "invariants" transforms {
            val invariants = setOf("sky", "news", "howe", "atlas", "cosmos", "bias", "andes")
            exec {
                if (it.length <= 2 || it in invariants) to(it, nextStep = null)
            }

            go("prelude")
        }

        "prelude" changes {
            toUppercase('Y') { isFirst() || precededBy(*vowels) }

            go("algorithm")
        }

        "algorithm" executes {
            val exceptions = arrayOf("gener", "commun", "arsen")
            regions {
                "1" { token ->
                    exceptions.find { token.startsWith(it) } ?.length
                            ?: token.indices(1)
                                    .firstOrNull { token[it] !in vowels && token[it - 1] in vowels }
                                    ?.let { it + 1 }
                }
                "2" { token ->
                    R["1"]?.let { r1 ->
                        token.indices(r1 + 1)
                                .firstOrNull { token[it] !in vowels && token[it - 1] in vowels }
                                ?.let { it + 1 }
                    }
                }
            }

            firstStep = "0" searches {
                suffix("'", "'s", "'s'") {
                    exec { delete(it) }
                }
                go("1a")
            }

            "1a" searches {
                suffix("sses") {
                    exec { replace(it, "ss") }
                }

                suffix("ied", "ies") {
                    exec {
                        if (it >= 1) replace(it, "i")
                        else replace(it, "ie")
                    }
                }

                suffix("us", "ss") {
                    exec { nop() }
                }

                suffix("s") {
                    exec {
                        if (it >= 2 && token.substring(0, it - 1).any { it in vowels }) delete(it)
                    }
                }

                go("invariants")
            }

            "invariants" transforms {
                val invariants = setOf("inning", "outing", "canning", "herring", "earring", "proceed", "exceed", "succeed")
                exec {
                    if (it in invariants) to(it, nextStep = null)
                }
                go("1b")
            }

            "1b" searches {
                suffix("eed", "eedly") {
                    exec { if (it in R1) replace(it, "ee") }
                }

                suffix("ed", "edly", "ing", "ingly") {
                    exec { if (token.substring(0, it).any { it in vowels }) delete(it, nextStep = "1b-after") }
                }

                go("1c")
            }

            "1b-after" searches {
                suffix("at", "bl", "iz") {
                    exec { add("e") }
                }

                suffix(*doubles) {
                    exec { delete(-1) }
                }

                find({
                    if (R1 >= it.length && it.endsWithShortSyllable()) 0
                    else -1
                }) {
                    exec { add("e") }
                }

                go("1c")
            }

            "1c" searches {
                suffix("y", "Y") {
                    exec {
                        if (it >= 2 && token[it - 1] !in vowels) replace(it, "i")
                    }
                }

                go("2")
            }

            "2" searches {
                suffix("tional") {
                    exec { if (it in R1) replace(it, "tion") }
                }

                suffix("enci") {
                    exec { if (it in R1) replace(it, "ence") }
                }

                suffix("anci") {
                    exec { if (it in R1) replace(it, "ance") }
                }

                suffix("abli") {
                    exec { if (it in R1) replace(it, "able") }
                }

                suffix("entli") {
                    exec { if (it in R1) replace(it, "ent") }
                }

                suffix("izer", "ization") {
                    exec { if (it in R1) replace(it, "ize") }
                }

                suffix("ational", "ation", "ator") {
                    exec { if (it in R1) replace(it, "ate") }
                }

                suffix("alism", "aliti", "alli") {
                    exec { if (it in R1) replace(it, "al") }
                }

                suffix("fulness") {
                    exec { if (it in R1) replace(it, "ful") }
                }

                suffix("ousli", "ousness") {
                    exec { if (it in R1) replace(it, "ous") }
                }

                suffix("iveness", "iviti") {
                    exec { if (it in R1) replace(it, "ive") }
                }

                suffix("biliti", "bli") {
                    exec { if (it in R1) replace(it, "ble") }
                }

                suffix("ogi") {
                    exec { if (it in R1 && token[it - 1] == 'l') replace(it, "og") }
                }

                suffix("fulli") {
                    exec { if (it in R1) replace(it, "ful") }
                }

                suffix("lessli") {
                    exec { if (it in R1) replace(it, "less") }
                }

                suffix("li") {
                    val endings = charArrayOf('c', 'd', 'e', 'g', 'h', 'k', 'm', 'n', 'r', 't')
                    exec { if (token[it - 1] in endings) delete(it) }
                }

                go("3")
            }

            "3" searches {
                suffix("tional") {
                    exec { if (it in R1) replace(it, "tion") }
                }

                suffix("ational") {
                    exec { if (it in R1) replace(it, "ate") }
                }

                suffix("alize") {
                    exec { if (it in R1) replace(it, "al") }
                }

                suffix("icate", "iciti", "ical") {
                    exec { if (it in R1) replace(it, "ic") }
                }

                suffix("ful", "ness") {
                    exec { if (it in R1) delete(it) }
                }

                suffix("ative") {
                    exec { if (it in R2) delete(it) }
                }

                go("4")
            }

            "4" searches {
                suffix("al", "ance", "ence", "er", "ic", "able", "ible", "ant", "ement", "ment", "ent", "ism", "ate", "iti", "ous", "ive", "ize") {
                    exec { if (it in R2) delete(it) }
                }

                suffix("ion") {
                    val endings = charArrayOf('s', 't')
                    exec { if (it in R2 && token[it - 1] in endings) delete(it) }
                }

                go("5")
            }

            "5" searches {
                suffix("e") {
                    exec {
                        if (it in R2) delete(it)
                        if (it in R1 && token.substring(0, it).endsWithShortSyllable().not()) delete(it)
                    }
                }
                suffix("l") {
                    exec {
                        if (it in R2 && token[it - 1] == 'l') delete(it)
                    }
                }

                go(null)
            }

            go("postlude")
        }

        "postlude" changes {
            toLowercase('Y')

            done()
        }

    }


    override val stopWords = setOf(
            "about", "above", "after", "again", "all", "also", "am", "an", "and", "another",
            "any", "are", "as", "at", "be", "because", "been", "before", "being", "below",
            "between", "both", "but", "by", "came", "can", "cannot", "come", "could", "did",
            "do", "does", "doing", "during", "each", "few", "for", "from", "further", "get",
            "got", "has", "had", "he", "have", "her", "here", "him", "himself", "his", "how",
            "if", "in", "into", "is", "it", "its", "itself", "like", "make", "many", "me",
            "might", "more", "most", "much", "must", "my", "myself", "never", "now", "of", "on",
            "only", "or", "other", "our", "ours", "ourselves", "out", "over", "own",
            "said", "same", "see", "should", "since", "so", "some", "still", "such", "take", "than",
            "that", "the", "their", "theirs", "them", "themselves", "then", "there", "these", "they",
            "this", "those", "through", "to", "too", "under", "until", "up", "very", "was",
            "way", "we", "well", "were", "what", "where", "when", "which", "while", "who",
            "whom", "with", "would", "why", "you", "your", "yours", "yourself"
    )

    override val accentsMap = "" to ""
}
