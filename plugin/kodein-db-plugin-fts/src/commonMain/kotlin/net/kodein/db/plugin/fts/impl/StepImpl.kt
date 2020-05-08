package net.kodein.db.plugin.fts.impl

import net.kodein.db.plugin.fts.RegionsMap
import net.kodein.db.plugin.fts.Stemmer

internal interface StepImpl : Stemmer.Step {
    data class Return(
            val token: String,
            val nextStep: String?
    )
    fun execute(token: String, regions: RegionsMap): Return
}
