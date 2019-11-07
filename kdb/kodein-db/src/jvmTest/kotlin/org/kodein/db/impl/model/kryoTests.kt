@file:Suppress("ClassName")

package org.kodein.db.impl.model

import org.kodein.db.TypeTable
import org.kodein.db.model.orm.Serializer
import org.kodein.db.orm.kryo.KryoSerializer
import org.kodein.db.withFullName

class ModelDBTests_00_PutGetDelete_Kryo : ModelDBTests_00_PutGetDelete() {
    override fun testSerializer(): Serializer<Any> = KryoSerializer()
    override fun testTypeTable(): TypeTable? = TypeTable.withFullName()
}

class ModelDBTests_01_Types_Kryo : ModelDBTests_01_Types() {
    override fun testSerializer(): Serializer<Any> = KryoSerializer()
    override fun testTypeTable(): TypeTable? = TypeTable.withFullName()
}

class ModelDBTests_02_IDs_Kryo : ModelDBTests_02_IDs() {
    override fun testSerializer(): Serializer<Any> = KryoSerializer()
    override fun testTypeTable(): TypeTable? = TypeTable.withFullName()
}

class ModelDBTests_03_Indexes_Kryo : ModelDBTests_03_Indexes() {
    override fun testSerializer(): Serializer<Any> = KryoSerializer()
    override fun testTypeTable(): TypeTable? = TypeTable.withFullName()
}

class ModelDBTests_04_Refs_Kryo : ModelDBTests_04_Refs() {
    override fun testSerializer(): Serializer<Any> = KryoSerializer()
    override fun testTypeTable(): TypeTable? = TypeTable.withFullName()
}

class ModelDBTests_05_Polymorphism_Kryo : ModelDBTests_05_Polymorphism() {
    override fun testSerializer(): Serializer<Any> = KryoSerializer()
}
