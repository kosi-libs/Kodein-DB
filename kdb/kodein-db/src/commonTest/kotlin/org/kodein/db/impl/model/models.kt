package org.kodein.db.impl.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.kodein.db.*
import org.kodein.db.model.ModelDB
import org.kodein.db.model.get
import org.kodein.db.model.orm.HasMetadata
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.util.UUID

@Serializable
data class Date(val day: Int, val month: Int, val year: Int)


interface Person : Metadata {
    val firstName: String
    val lastName: String
    val birth: Date

    val fullName get() = "$firstName $lastName"

    override val id get() = listOf(lastName, firstName)
    override fun indexes(): Set<Index> = indexSet(
            "firstName" to firstName,
            "birth" to listOf(birth.year, birth.month, birth.day)
    )
}

@Serializable
data class Adult(override val firstName: String, override val lastName: String, override val birth: Date) : Person

@Serializable
data class Child(override val firstName: String, override val lastName: String, override val birth: Date, val parents: Pair<Key<Adult>, Key<Adult>>) : Person

@Serializable
data class Location(val lat: Double, val lng: Double)

@Serializable
data class City(val name: String, val location: Location, val postalCode: Int) : Metadata {
    override val id get() = postalCode
    override fun indexes(): Set<Index> = indexSet("name" to name)
}

@Serializable
data class Message(@Contextual override val id: UUID, val from: Key<Person>, val message: String) : Metadata

@Serializable
data class Birth(val adult: Key<Adult>, val city: Key<City>) : HasMetadata {
    override fun getMetadata(db: ModelDB, vararg options: Options.Write): Metadata {
        val person = db[adult]!!
        return Metadata(person.model.id,
                "city" to db[city]!!.model.name,
                "date" to listOf(person.model.birth.year, person.model.birth.month, person.model.birth.day)
        )
    }
}
