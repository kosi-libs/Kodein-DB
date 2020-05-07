//package net.kodein.db.plugin.fts.lang
//
//import net.kodein.db.plugin.fts.stemmer
//
//val enStemmer = stemmer {
//
//    exceptions {
//        when (it) {
//            "skis" -> "ski"
//            "skies" -> "sky"
//            "dying" -> "die"
//            "lying" -> "lie"
//            "tying" -> "tie"
//            "idly" -> "idl"
//            "gently" -> "gentl"
//            "ugly" -> "ugli"
//            "only" -> "onli"
//            "singly" -> "singl"
//        }
//    }
//
//    invariants(maxSize = 3)
//    invariants("news", "howe", "atlas", "cosmos", "bias", "andes")
//
//    algo {
//
//    }
//
//}
