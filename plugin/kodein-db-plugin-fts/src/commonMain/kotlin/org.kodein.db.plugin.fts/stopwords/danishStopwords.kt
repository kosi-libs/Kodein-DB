package org.kodein.db.plugin.fts.stopwords

@OptIn(ExperimentalStdlibApi::class)
internal val danishStopwords by lazy { buildSet {
    addAll(arrayOf("ad", "af", "aldrig", "alle", "alt", "anden", "andet", "andre", "at", "bare"))
    addAll(arrayOf("begge", "blev", "blive", "bliver", "da", "de", "dem", "den", "denne", "der"))
    addAll(arrayOf("deres", "det", "dette", "dig", "din", "dine", "disse", "dit", "dog", "du"))
    addAll(arrayOf("efter", "ej", "eller", "en", "end", "ene", "eneste", "enhver", "er", "et"))
    addAll(arrayOf("far", "fem", "fik", "fire", "flere", "fleste", "for", "fordi", "forrige", "fra"))
    addAll(arrayOf("få", "får", "før", "god", "godt", "ham", "han", "hans", "har", "havde"))
    addAll(arrayOf("have", "hej", "helt", "hende", "hendes", "her", "hos", "hun", "hvad", "hvem"))
    addAll(arrayOf("hver", "hvilken", "hvis", "hvor", "hvordan", "hvorfor", "hvornår", "i", "ikke", "ind"))
    addAll(arrayOf("ingen", "intet", "ja", "jeg", "jer", "jeres", "jo", "kan", "kom", "komme"))
    addAll(arrayOf("kommer", "kun", "kunne", "lad", "lav", "lidt", "lige", "lille", "man", "mand"))
    addAll(arrayOf("mange", "med", "meget", "men", "mens", "mere", "mig", "min", "mine", "mit"))
    addAll(arrayOf("mod", "må", "ned", "nej", "ni", "nogen", "noget", "nogle", "nu", "ny"))
    addAll(arrayOf("nyt", "når", "nær", "næste", "næsten", "og", "også", "okay", "om", "op"))
    addAll(arrayOf("os", "otte", "over", "på", "se", "seks", "selv", "ser", "ses", "sig"))
    addAll(arrayOf("sige", "sin", "sine", "sit", "skal", "skulle", "som", "stor", "store", "syv"))
    addAll(arrayOf("så", "sådan", "tag", "tage", "thi", "ti", "til", "to", "tre", "ud"))
    addAll(arrayOf("under", "var", "ved", "vi", "vil", "ville", "vor", "vores", "være", "været"))
}}
