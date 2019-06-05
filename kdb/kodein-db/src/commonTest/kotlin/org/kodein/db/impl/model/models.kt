package org.kodein.db.impl.model

import kotlinx.serialization.Serializable
import org.kodein.db.Index
import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.db.indexSet
import org.kodein.db.model.HasMetadata
import org.kodein.db.model.Metadata
import org.kodein.db.model.ModelDB
import org.kodein.db.orm.kotlinx.Ref
import org.kodein.db.orm.kotlinx.get

@Serializable
data class Date(val day: Int, val month: Int, val year: Int)


interface Person : Metadata {
    val firstName: String
    val lastName: String
    val birth: Date

    override val primaryKey: Value get() = Value.ofAscii(lastName, firstName)
    override val indexes: Set<Index> get() = indexSet(
            "firstName" to Value.ofAscii(firstName),
            "birth" to Value.of(birth.year, birth.month, birth.day)
    )
}

@Serializable
data class Adult(override val firstName: String, override val lastName: String, override val birth: Date) : Metadata, Person

@Serializable
data class Child(override val firstName: String, override val lastName: String, override val birth: Date, val parents: Pair<Ref<Adult>, Ref<Adult>>) : Metadata, Person

@Serializable
data class Location(val lat: Double, val lng: Double)

@Serializable
data class City(val name: String, val location: Location, val postalCode: Int) : Metadata {
    override val primaryKey get() = Value.ofAscii(name)
}

@Serializable
data class Birth(val adult: Ref<Adult>, val city: Ref<City>) : HasMetadata {
    override fun getMetadata(db: ModelDB, vararg options: Options.Write): Metadata {
        val person = db[adult]!!
        return Metadata(
                primaryKey = person.value.primaryKey,
                indexes = indexSet(
                        "city" to Value.ofAscii(db[city]!!.value.name),
                        "date" to Value.of(person.value.birth.year, person.value.birth.month, person.value.birth.day)
                )
        )
    }
}
