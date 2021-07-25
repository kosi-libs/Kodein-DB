package org.kodein.db.impl.index

import kotlinx.serialization.Serializable
import org.kodein.db.index.ModelIndexes
import org.kodein.db.model.orm.Metadata

@Serializable
data class IndexCity(val name: String, val country: String, val postalCode: Int) : Metadata {
    override val id get() = postalCode

    override fun indexes() = Indexes.of(this)

    object Indexes : ModelIndexes<IndexCity>() {
        val name by index { name }
        val country by index { country }

        val nameCountryPostalCode by indexTriple { Triple(name, country, postalCode) }
    }
}
