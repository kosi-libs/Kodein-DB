package net.kodein.db.plugin.fts.lang

import net.kodein.db.plugin.fts.Stemmer
import net.kodein.db.plugin.fts.stemmer
import net.kodein.db.plugin.fts.unAccentedStemmer
import net.kodein.db.plugin.fts.util.indices
import net.kodein.db.plugin.fts.util.prefixed


private val Stemmer.Step.Exec.RV get() = R.getValue(V)
private val Stemmer.Step.Exec.R1 get() = R.getValue(1)
private val Stemmer.Step.Exec.R2 get() = R.getValue(2)


@OptIn(ExperimentalStdlibApi::class)
private val frStemmerBuilder: Stemmer.() -> Unit = {
    val vowels = "aeiouyâàëéêèïîôûù".toCharArray()

    prelude {
        toUppercase('u', 'i') { precededBy(*vowels) && followedBy(*vowels) }
        toUppercase('y') { precededBy(*vowels) || followedBy(*vowels) }
        toUppercase('u') { precededBy('q') }
        replace('ë', "He")
        replace('ï', "Hi")
    }

    regions {
        V { word ->
            when {
                word[0] in vowels && word[1] in vowels -> 3
                word.prefixed("pal", "col", "tap") -> 3
                else -> word.indexOfAny(vowels, 1).takeIf { it != -1 } ?.let { it + 1 }
            }
        }
        1 { word ->
            word.indices(1).firstOrNull {
                word[it] !in vowels && word[it - 1] in vowels
            } ?.let { it + 1 }
        }
        2 { word ->
            R[1]?.let { r1 ->
                word.indices(r1 + 1).firstOrNull { word[it] !in vowels && word[it - 1] in vowels } ?.let { it + 1 }
            }
        }
    }

    steps {
        firstStep = 10 {
            suffix("ance", "iqUe", "isme", "able", "iste", "eux", "ances", "iqUes", "ismes", "ables", "istes") {
                if (it >= R2) delete()
            }
            suffix("atrice", "ateur", "ation", "atrices", "ateurs", "ations") {
                if (it >= R2) delete()
                precededBy("ic") {
                    if (it >= R2) delete()
                    else replaceWith("iqU")
                }
            }
            suffix("logie", "logies") {
                if (it >= R2) replaceWith("log")
            }
            suffix("usion", "ution", "usions", "utions") {
                if (it >= R2) replaceWith("u")
            }
            suffix("ence", "ences") {
                if (it >= R2) replaceWith("ent")
            }
            suffix("ement", "ements") {
                if (it >= RV) delete()
                precededBy("iv") {
                    if (it >= R2) delete()
                    precededBy("at") {
                        if (it >= R2) delete()
                    }
                }
                precededBy("eus") {
                    if (it >= R2) delete()
                    else if (it >= R1) replaceWith("eux")
                }
                precededBy("abl", "iqU") {
                    if (it >= R2) delete()
                }
                precededBy("ièr", "Ièr") {
                    if (it >= RV) replaceWith("i")
                }
            }
            suffix("ité", "ités") {
                if (it >= R2) delete()
                precededBy("abil") {
                    if (it >= R2) delete()
                    else replaceWith("abl")
                }
                precededBy("ic") {
                    if (it >= R2) delete()
                    else replaceWith("iqU")
                }
                precededBy("iv") {
                    if (it >= R2) delete()
                }
            }
            suffix("if", "ive", "ifs", "ives") {
                if (it >= R2) delete()
                precededBy("at") {
                    if (it >= R2) delete()
                    precededBy("ic") {
                        if (it >= R2) delete()
                        else replaceWith("iqU")
                    }
                }
            }
            suffix("eaux") {
                replaceWith("eau")
            }
            suffix("aux") {
                if (it >= R1) replaceWith("al")
            }
            suffix("euse", "euses") {
                if (it >= R2) delete()
                else if (it >= R1) replaceWith("eux")
            }
            suffix("issement", "issements") {
                if (it >= R1 && token[it - 1] !in vowels) delete()
            }
            suffix("amment") {
                if (it >= RV) replaceWith("ant", nextStep = 20)
            }
            suffix("emment") {
                if (it >= RV) replaceWith("ant", nextStep = 20)
            }
            suffix("ment", "ments") {
                if (it >= RV + 1 && token[it - 1] in vowels) delete(nextStep = 20)
            }
            nextStep(noChange = 20, changed = 30)
        }
        20 {
            suffix("îmes", "ît", "îtes", "i", "ie", "ies", "ir", "ira", "irai", "iraIent", "irais", "irait", "iras", "irent", "irez", "iriez", "irions", "irons", "iront", "is", "issaIent", "issais", "issait", "issant", "issante", "issantes", "issants", "isse", "issent", "isses", "issez", "issiez", "issions", "issons", "it") {
                if (it >= RV + 1 && token[it - 1] !in vowels && token[it -1] != 'H') delete()
            }
            suffix("ions") {
                if (it >= R2) delete()
            }
            suffix("é", "ée", "ées", "és", "èrent", "er", "era", "erai", "eraIent", "erais", "erait", "eras", "erez", "eriez", "erions", "erons", "eront", "ez", "iez") {
                if (it >= RV) delete()
            }
            suffix("âmes", "ât", "âtes", "a", "ai", "aIent", "ais", "ait", "ant", "ante", "antes", "ants", "as", "asse", "assent", "asses", "assiez", "assions") {
                if (it >= RV) delete()
                precededBy("e") {
                    if (it >= RV) delete()
                }
            }
            nextStep(noChange = 40, changed = 30)
        }
        30 {
            suffix("Y") { replaceWith("i") }
            suffix("ç") { replaceWith("c") }
            nextStep(50)
        }
        40 {
            suffix("s") {
                if (it >= 1 && token[it - 1] !in "aiouès") delete()
                if (it >= 2 && token[it - 1] == 'i' && token[it - 2] == 'H') delete()
            }
            nextStep(41)
        }
        41 {
            suffix("ion") {
                if (it >= R2 && token[it - 1] !in "st") delete()
            }
            suffix("ier", "ière", "Ier", "Ière") {
                replaceWith("i")
            }
            suffix("e") {
                delete()
            }
            nextStep(50)
        }
        50 {
            suffix("enn", "onn", "ett", "ell", "eill") {
                delete(chars = 1)
            }
            nextStep(60)
        }
        60 {
            findSuffix({
                var i = it.length - 1
                while (i >= 0) {
                    if (it[i] !in vowels) --i
                    else if (i != it.lastIndex && it[i] in "éè") return@findSuffix i
                    else break
                }
                -1
            }) {
                replaceWith("e" + token.substring(it + 1))
            }
            nextStep(end)
        }
    }

    postlude {
        toLowercase('I', 'U', 'Y')
        compact('H') {
            and('e', "ë")
            and('i', "ï")
            default("")
        }
    }
}


val frStemmer = stemmer(frStemmerBuilder)

val frAccents =
        "âàëéêèïîôûù" to
        "aaeeeeiiouu"

val frUnaccentedStemmer = unAccentedStemmer(frAccents, frStemmerBuilder)

val frStopWords = setOf(
        "a",
        "au",
        "aux",
        "avec",
        "ce",
        "ces",
        "dans",
        "de",
        "des",
        "du",
        "elle",
        "en",
        "et",
        "eux",
        "il",
        "je",
        "la",
        "le",
        "leur",
        "lui",
        "ma",
        "mais",
        "me",
        "même",
        "mes",
        "moi",
        "mon",
        "ne",
        "nos",
        "notre",
        "nous",
        "on",
        "ou",
        "où",
        "par",
        "pas",
        "pour",
        "qu",
        "que",
        "qui",
        "sa",
        "se",
        "ses",
        "son",
        "sur",
        "ta",
        "te",
        "tes",
        "toi",
        "ton",
        "tu",
        "un",
        "une",
        "vos",
        "votre",
        "vous",
        "c",
        "d",
        "j",
        "l",
        "à",
        "m",
        "n",
        "s",
        "t",
        "y",
        "été",
        "étée",
        "étées",
        "étés",
        "étant",
        "suis",
        "es",
        "est",
        "sommes",
        "êtes",
        "sont",
        "serai",
        "seras",
        "sera",
        "serons",
        "serez",
        "seront",
        "serais",
        "serait",
        "serions",
        "seriez",
        "seraient",
        "étais",
        "était",
        "étions",
        "étiez",
        "étaient",
        "fus",
        "fut",
        "fûmes",
        "fûtes",
        "furent",
        "sois",
        "soit",
        "soyons",
        "soyez",
        "soient",
        "fusse",
        "fusses",
        "fût",
        "fussions",
        "fussiez",
        "fussent",
        "ayant",
        "eu",
        "eue",
        "eues",
        "eus",
        "ai",
        "as",
        "avons",
        "avez",
        "ont",
        "aurai",
        "auras",
        "aura",
        "aurons",
        "aurez",
        "auront",
        "aurais",
        "aurait",
        "aurions",
        "auriez",
        "auraient",
        "avais",
        "avait",
        "avions",
        "aviez",
        "avaient",
        "eut",
        "eûmes",
        "eûtes",
        "eurent",
        "aie",
        "aies",
        "ait",
        "ayons",
        "ayez",
        "aient",
        "eusse",
        "eusses",
        "eût",
        "eussions",
        "eussiez",
        "eussent",
        "ceci",
        "cela",
        "cet",
        "cette",
        "ici",
        "ils",
        "les",
        "leurs",
        "quel",
        "quels",
        "quelle",
        "quelles",
        "sans",
        "soi"
)
