@file:Suppress("ClassName")

package org.kodein.db.impl.model

import org.kodein.db.TypeTable
import org.kodein.db.model.orm.DefaultSerializer
import org.kodein.db.orm.kryo.KryoSerializer
import org.kodein.db.withFullName

class ModelDBTests_00_PutGetDelete_Kryo : ModelDBTests_00_PutGetDelete() {
    override fun testSerializer(): DefaultSerializer = KryoSerializer()
    override fun testTypeTable() = TypeTable.withFullName()
}

class ModelDBTests_01_Types_Kryo : ModelDBTests_01_Types() {
    override fun testSerializer(): DefaultSerializer = KryoSerializer()
    override fun testTypeTable() = TypeTable.withFullName()
}

class ModelDBTests_02_IDs_Kryo : ModelDBTests_02_IDs() {
    override fun testSerializer(): DefaultSerializer = KryoSerializer()
    override fun testTypeTable() = TypeTable.withFullName()
}

class ModelDBTests_03_Indexes_Kryo : ModelDBTests_03_Indexes() {
    override fun testSerializer(): DefaultSerializer = KryoSerializer()
    override fun testTypeTable() = TypeTable.withFullName()
}

class ModelDBTests_04_Refs_Kryo : ModelDBTests_04_Refs() {
    override fun testSerializer(): DefaultSerializer = KryoSerializer()
    override fun testTypeTable() = TypeTable.withFullName()
}

class ModelDBTests_05_Polymorphism_Kryo : ModelDBTests_05_PolymorphicTypeTable() {
    override fun testSerializer(): DefaultSerializer = KryoSerializer()
    override fun testTypeTable() = TypeTable.withFullName {
        root<Person>()
                .sub<Adult>()
                .sub<Child>()
    }
}

class ModelDBTests_06_All_Kryo : ModelDBTests_06_All() {
    override fun testSerializer(): DefaultSerializer = KryoSerializer()
    override fun testTypeTable() = TypeTable.withFullName()
}

class ModelDBTests_07_React_Kryo : ModelDBTests_07_React() {
    override fun testSerializer(): DefaultSerializer = KryoSerializer()
    override fun testTypeTable() = TypeTable.withFullName()
}

class ModelDBTests_08_Primitives_Kryo : ModelDBTests_08_Primitives() {
    override fun testSerializer(): DefaultSerializer = KryoSerializer()
    override fun testTypeTable() = TypeTable.withFullName()
}

class ModelDBTests_09_Checks_Kryo : ModelDBTests_09_Checks() {
    override fun testSerializer(): DefaultSerializer = KryoSerializer()
    override fun testTypeTable() = TypeTable.withFullName()
}
