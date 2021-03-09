package org.kodein.db.plugin.fts

import kotlinx.serialization.Serializable
import org.kodein.db.model.orm.Metadata


@Serializable
data class Contact(override val id: Int, val name: String, val city: String) : Metadata, HasFullText {
    override fun texts(): FtsTexts = mapOf(
        "name" to name.ftsTokens()
    )
}

