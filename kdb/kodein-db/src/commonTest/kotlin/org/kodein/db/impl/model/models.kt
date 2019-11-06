package org.kodein.db.impl.model

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import org.kodein.db.*
import org.kodein.db.model.ModelDB
import org.kodein.db.model.get
import org.kodein.db.model.orm.HasMetadata
import org.kodein.db.model.orm.Metadata

@Serializable
data class Date(val day: Int, val month: Int, val year: Int)


interface Person : Metadata {
    val firstName: String
    val lastName: String
    val birth: Date

    override val id: Value get() = Value.ofAscii(lastName, firstName)
    override val indexes: Set<Index> get() = indexSet(
            "firstName" to Value.ofAscii(firstName),
            "birth" to Value.of(birth.year, birth.month, birth.day)
    )
}

@Serializable
data class Adult(override val firstName: String, override val lastName: String, override val birth: Date) : Person

@Serializable
data class Child(override val firstName: String, override val lastName: String, override val birth: Date, val parents: Pair<@ContextualSerialization Key<Adult>, @ContextualSerialization Key<Adult>>) : Person

@Serializable
data class Location(val lat: Double, val lng: Double)

@Serializable
data class City(val name: String, val location: Location, val postalCode: Int) : Metadata {
    override val id get() = Value.ofAscii(name)
}

@Serializable
data class Birth(@ContextualSerialization val adult: Key<Adult>, @ContextualSerialization val city: Key<City>) : HasMetadata {
    override fun getMetadata(db: ModelDB, vararg options: Options.Write): Metadata {
        val person = db[adult]!!
        return Metadata(
                id = person.model.id,
                indexes = indexSet(
                        "city" to Value.ofAscii(db[city]!!.model.name),
                        "date" to Value.of(person.model.birth.year, person.model.birth.month, person.model.birth.day)
                )
        )
    }
}
