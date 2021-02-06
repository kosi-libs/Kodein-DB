package org.kodein.db.plugin.fts

import org.kodein.db.plugin.fts.stemmer.*


public data class FtsLang(val stemmer: () -> Stemmer, val stopwords: List<String>) {

    private object All {
        val map = mapOf(
            "danish" to danish,
            "dutch" to dutch,
            "english" to english,
            "finnish" to finnish,
            "french" to french,
            "german" to german,
            "hungarian" to hungarian,
            "italian" to italian,
            "norwegian" to norwegian,
            "portuguese" to portuguese,
            "romanian" to romanian,
            "russian" to russian,
            "spanish" to spanish,
            "swedish" to swedish,
            "turkish" to turkish,
        )
    }

    public companion object : Map<String, FtsLang> by All.map {
        public val danish: FtsLang = FtsLang(::DanishStemmer, danishStopwords)
        public val dutch: FtsLang = FtsLang(::DutchStemmer, dutchStopwords)
        public val english: FtsLang = FtsLang(::EnglishStemmer, englishStopwords)
        public val finnish: FtsLang = FtsLang(::FinnishStemmer, finnishStopwords)
        public val french: FtsLang = FtsLang(::FrenchStemmer, frenchStopwords)
        public val german: FtsLang = FtsLang(::GermanStemmer, germanStopwords)
        public val hungarian: FtsLang = FtsLang(::HungarianStemmer, hungarianStopwords)
        public val italian: FtsLang = FtsLang(::ItalianStemmer, italianStopwords)
        public val norwegian: FtsLang = FtsLang(::NorwegianStemmer, norwegianStopwords)
        public val portuguese: FtsLang = FtsLang(::PortugueseStemmer, portugueseStopwords)
        public val romanian: FtsLang = FtsLang(::RomanianStemmer, romanianStopwords)
        public val russian: FtsLang = FtsLang(::RussianStemmer, russianStopwords)
        public val spanish: FtsLang = FtsLang(::SpanishStemmer, spanishStopwords)
        public val swedish: FtsLang = FtsLang(::SwedishStemmer, swedishStopwords)
        public val turkish: FtsLang = FtsLang(::TurkishStemmer, turkishStopwords)
    }

}
