package org.kodein.db.plugin.fts

import org.kodein.db.plugin.fts.stemmer.*
import org.kodein.db.plugin.fts.stopwords.*


public interface Lang {

    public fun stemmer(): Stemmer
    public fun stopwords(): Set<String>

    private object All {
        val map = mapOf(
            "danish" to Danish,
            "dutch" to Dutch,
            "english" to English,
            "finnish" to Finnish,
            "french" to French,
            "german" to German,
            "hungarian" to Hungarian,
            "italian" to Italian,
            "norwegian" to Norwegian,
            "portuguese" to Portuguese,
            "romanian" to Romanian,
            "russian" to Russian,
            "spanish" to Spanish,
            "swedish" to Swedish,
            "turkish" to Turkish,
        )
    }

    public companion object : Map<String, Lang> by All.map {
        public operator fun invoke(stemmer: () -> Stemmer, stopwords: () -> Set<String>): Lang = object : Lang {
            override fun stemmer() = stemmer()
            override fun stopwords() = stopwords()
        }
    }

}

public val Danish: Lang = Lang(::DanishStemmer, ::danishStopwords)
public val Dutch: Lang = Lang(::DutchStemmer, ::dutchStopwords)
public val English: Lang = Lang(::EnglishStemmer, ::englishStopwords)
public val Finnish: Lang = Lang(::FinnishStemmer, ::finnishStopwords)
public val French: Lang = Lang(::FrenchStemmer, ::frenchStopwords)
public val German: Lang = Lang(::GermanStemmer, ::germanStopwords)
public val Hungarian: Lang = Lang(::HungarianStemmer, ::hungarianStopwords)
public val Italian: Lang = Lang(::ItalianStemmer, ::italianStopwords)
public val Norwegian: Lang = Lang(::NorwegianStemmer, ::norwegianStopwords)
public val Portuguese: Lang = Lang(::PortugueseStemmer, ::portugueseStopwords)
public val Romanian: Lang = Lang(::RomanianStemmer, ::romanianStopwords)
public val Russian: Lang = Lang(::RussianStemmer, ::russianStopwords)
public val Spanish: Lang = Lang(::SpanishStemmer, ::spanishStopwords)
public val Swedish: Lang = Lang(::SwedishStemmer, ::swedishStopwords)
public val Turkish: Lang = Lang(::TurkishStemmer, ::turkishStopwords)
