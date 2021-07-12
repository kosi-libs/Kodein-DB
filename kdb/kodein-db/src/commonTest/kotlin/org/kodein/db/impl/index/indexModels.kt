package org.kodein.db.impl.index

import kotlinx.serialization.Serializable
import org.kodein.db.index.index
import org.kodein.db.index.indexMapOf
import org.kodein.db.model.orm.Metadata

@Serializable
data class IndexCity(val name: String, val country: String, val postalCode: Int) : Metadata {
    override val id get() = postalCode

    val nameIndex by index(name)
    val countryIndex by index(country)

    val nameCountryPostalCodeIndex by index(name, country, postalCode)

    override fun indexes() = indexMapOf(nameIndex, countryIndex, nameCountryPostalCodeIndex)
}
