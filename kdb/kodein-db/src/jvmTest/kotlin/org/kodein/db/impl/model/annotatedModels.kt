package org.kodein.db.impl.model

import org.kodein.db.Key
import org.kodein.db.Value
import org.kodein.db.model.Id
import org.kodein.db.model.Indexed
import org.kodein.db.model.PolymorphicCollection
import org.kodein.memory.util.UUID


data class ADate(val day: Int, val month: Int, val year: Int)

interface APerson {
    @get:Id val id: Int
    @get:Indexed("firstName") val firstName: String
    val lastName: String
    val birth: Date

    val fullName get() = "$firstName $lastName"

    @Indexed("birth") fun birthIndex() = Value.of(birth.year, birth.month, birth.day)
}

@PolymorphicCollection(APerson::class)
data class AAdult(override val id: Int, override val firstName: String, override val lastName: String, override val birth: Date) : APerson

@PolymorphicCollection(APerson::class)
data class AChild(override val id: Int, override val firstName: String, override val lastName: String, override val birth: Date, val parents: Pair<Key<AAdult>, Key<AAdult>>) : APerson

//data class ALocation(@Id val id: Int, val lat: Double, val lng: Double)
//
//data class ACity(@Id val id: Int, @Indexed("name") val name: String, val location: Location, val postalCode: Int)
//
//data class AMessage(@Id val uid: UUID, val from: Key<Person>, val message: String)
