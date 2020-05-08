package net.kodein.db.plugin.fts.lang

import net.kodein.db.plugin.fts.Stemmer
import net.kodein.db.plugin.fts.stemmer
import net.kodein.db.plugin.fts.unAccentedStemmer
import net.kodein.db.plugin.fts.unaccented
import net.kodein.db.plugin.fts.util.indices
import net.kodein.db.plugin.fts.util.prefixed


private val Stemmer.Searches.Exec.RV get() = R.getValue("V")
private val Stemmer.Searches.Exec.R1 get() = R.getValue("1")
private val Stemmer.Searches.Exec.R2 get() = R.getValue("2")

private val frStemmerBuilder: Stemmer.Algorithm.() -> Unit = {

    val vowels = charArrayOf('a', 'e', 'i', 'o', 'u', 'y', 'â', 'à', 'ë', 'é', 'ê', 'è', 'ï', 'î', 'ô', 'û', 'ù')

    firstStep = "prelude" changes {
        toUppercase('u', 'i') { precededBy(*vowels) && followedBy(*vowels) }
        toUppercase('y') { precededBy(*vowels) || followedBy(*vowels) }
        toUppercase('u') { precededBy('q') }
        replace('ë', "He")
        replace('ï', "Hi")

        go("algorithm")
    }

    "algorithm" executes {
        regions {
            "V" { token ->
                when {
                    token[0] in vowels && token[1] in vowels -> 3
                    token.prefixed("pal", "col", "tap") -> 3
                    else -> token.indexOfAny(vowels, 1).takeIf { it != -1 } ?.let { it + 1 }
                }
            }
            "1" { token ->
                token.indices(1)
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

        firstStep = "1" searches {
            suffix("ance", "iqUe", "isme", "able", "iste", "eux", "ances", "iqUes", "ismes", "ables", "istes") {
                exec { if (it in R2) delete(it) }
            }
            suffix("atrice", "ateur", "ation", "atrices", "ateurs", "ations") {
                exec { if (it in R2) delete(it) }
                precededBy("ic") {
                    exec {
                        if (it in R2) delete(it)
                        else replace(it, "iqU")
                    }
                }
            }
            suffix("logie", "logies") {
                exec { if (it in R2) replace(it, "log") }
            }
            suffix("usion", "ution", "usions", "utions") {
                exec { if (it in R2) replace(it, "u") }
            }
            suffix("ence", "ences") {
                exec { if (it in R2) replace(it, "ent") }
            }
            suffix("ement", "ements") {
                exec { if (it in RV) delete(it) }
                precededBy("iv") {
                    exec { if (it in R2) delete(it) }
                    precededBy("at") {
                        exec { if (it in R2) delete(it) }
                    }
                }
                precededBy("eus") {
                    exec {
                        if (it in R2) delete(it)
                        else if (it in R1) replace(it, "eux")
                    }
                }
                precededBy("abl", "iqU") {
                    exec { if (it in R2) delete(it) }
                }
                precededBy("ièr", "Ièr") {
                    exec { if (it in RV) replace(it, "i") }
                }
            }
            suffix("ité", "ités") {
                exec { if (it in R2) delete(it) }
                precededBy("abil") {
                    exec {
                        if (it in R2) delete(it)
                        else replace(it, "abl")
                    }
                }
                precededBy("ic") {
                    exec {
                        if (it in R2) delete(it)
                        else replace(it, "iqU")
                    }
                }
                precededBy("iv") {
                    exec { if (it in R2) delete(it) }
                }
            }
            suffix("if", "ive", "ifs", "ives") {
                exec { if (it in R2) delete(it) }
                precededBy("at") {
                    exec { if (it in R2) delete(it) }
                    precededBy("ic") {
                        exec {
                            if (it in R2) delete(it)
                            else replace(it, "iqU")
                        }
                    }
                }
            }
            suffix("eaux") {
                exec { replace(it, "eau") }
            }
            suffix("aux") {
                exec { if (it in R1) replace(it, "al") }
            }
            suffix("euse", "euses") {
                exec {
                    if (it in R2) delete(it)
                    else if (it in R1) replace(it, "eux")
                }
            }
            suffix("issement", "issements") {
                exec { if (it in R1 && token[it - 1] !in vowels) delete(it) }
            }
            suffix("amment") {
                exec { if (it in RV) replace(it, "ant", nextStep = "2") }
            }
            suffix("emment") {
                exec { if (it in RV) replace(it, "ant", nextStep = "2") }
            }
            suffix("ment", "ments") {
                exec { if (it in RV + 1 && token[it - 1] in vowels) delete(it, nextStep = "2") }
            }

            go(noChange = "2a", changed = "3")
        }

        "2a" searches {
            suffix("îmes", "ît", "îtes", "i", "ie", "ies", "ir", "ira", "irai", "iraIent", "irais", "irait", "iras", "irent", "irez", "iriez", "irions", "irons", "iront", "is", "issaIent", "issais", "issait", "issant", "issante", "issantes", "issants", "isse", "issent", "isses", "issez", "issiez", "issions", "issons", "it") {
                exec { if (it in RV + 1 && token[it - 1] !in vowels && token[it -1] != 'H') delete(it) }
            }

            go(noChange = "2b", changed = "3")
        }

        "2b" searches {
            suffix("ions") {
                exec { if (it in R2) delete(it) }
            }
            suffix("é", "ée", "ées", "és", "èrent", "er", "era", "erai", "eraIent", "erais", "erait", "eras", "erez", "eriez", "erions", "erons", "eront", "ez", "iez") {
                exec { if (it in RV) delete(it) }
            }
            suffix("âmes", "ât", "âtes", "a", "ai", "aIent", "ais", "ait", "ant", "ante", "antes", "ants", "as", "asse", "assent", "asses", "assiez", "assions") {
                exec { if (it in RV) delete(it) }
                precededBy("e") {
                    exec { if (it in RV) delete(it) }
                }
            }

            go(noChange = "4a", changed = "3")
        }

        "3" searches {
            suffix("Y") { exec { replace(it, "i") } }
            suffix("ç") { exec { replace(it, "c") } }

            go("5")
        }

        "4a" searches {
            suffix("s") {
                exec {
                    if (it in 1 && token[it - 1] !in "aiouès") delete(it)
                    if (it in 2 && token[it - 1] == 'i' && token[it - 2] == 'H') delete(it)
                }
            }

            go("4b")
        }

        "4b" searches {
            suffix("ion") {
                exec { if (it in R2 && token[it - 1] !in "st") delete(it) }
            }
            suffix("ier", "ière", "Ier", "Ière") {
                exec { replace(it, "i") }
            }
            suffix("e") {
                exec { delete(it) }
            }

            go("5")
        }

        "5" searches {
            suffix("enn", "onn", "ett", "ell", "eill") {
                exec { delete(-1) }
            }

            go("6")
        }

        "6" searches {
            find({
                var i = it.length - 1
                while (i >= 0) {
                    if (it[i] !in vowels) --i
                    else if (i != it.lastIndex && it[i] in "éè") return@find i
                    else break
                }
                -1
            }) {
                exec { replace(it, "e" + token.substring(it + 1)) }
            }

            done()
        }

        go("postlude")
    }

    "postlude" changes {
        toLowercase('I', 'U', 'Y')
        compact('H') {
            and('e', "ë")
            and('i', "ï")
            default("")
        }

        done()
    }
}


object FR : Lang {
    override val stemmer = stemmer(frStemmerBuilder)

    override val stopWords = setOf(
            "au", "aux", "avec", "ce", "ces", "dans", "de", "des", "du", "elle",
            "en", "et", "eux", "il", "je", "la", "le", "leur", "lui",  "ma",
            "mais", "me", "même", "mes", "moi", "mon", "ne", "nos", "notre", "nous",
            "on", "ou", "où", "par", "pas", "pour", "qu", "que", "qui", "sa",
            "se", "ses", "son", "sur", "ta", "te", "tes", "toi", "ton", "tu",
            "un", "une", "vos", "votre", "vous", "été", "étée", "étées", "étés", "étant",
            "suis", "es", "est", "sommes", "êtes", "sont", "serai", "seras", "sera", "serons",
            "serez", "seront", "serais", "serait", "serions", "seriez", "seraient", "étais", "était", "étions",
            "étiez", "étaient", "fus", "fut", "fûmes", "fûtes", "furent", "sois", "soit", "soyons",
            "soyez", "soient", "fusse", "fusses", "fût", "fussions", "fussiez", "fussent", "ayant", "eu",
            "eue", "eues", "eus", "ai", "as", "avons", "avez", "ont", "aurai", "auras",
            "aura", "aurons", "aurez", "auront", "aurais", "aurait", "aurions", "auriez", "auraient", "avais",
            "avait", "avions", "aviez", "avaient", "eut", "eûmes", "eûtes", "eurent", "aie", "aies",
            "ait", "ayons", "ayez", "aient", "eusse", "eusses", "eût", "eussions", "eussiez", "eussent",
            "ceci", "cela", "cet", "cette", "ici", "ils", "les", "leurs", "quel", "quels",
            "quelle", "quelles", "sans", "soi"
    )

    override val accentsMap =
            "âàëéêèïîôûù" to
            "aaeeeeiiouu"
}

object FRUnaccented : Lang {
    override val stemmer = unAccentedStemmer(FR.accentsMap, frStemmerBuilder)

    override val stopWords = FR.stopWords.map { it.unaccented(FR.accentsMap) } .toSet()

    override val accentsMap get() = FR.accentsMap
}
