@file:Suppress("ClassName")

package org.kodein.db.impl.model

import org.kodein.db.TypeTable
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.model.ModelDB
import org.kodein.db.model.orm.DefaultSerializer
import org.kodein.db.orm.kryo.KryoSerializer
import org.kodein.db.withFullName
import org.kodein.memory.file.FileSystem

abstract class ModelDBTests_00_PutGetDelete_Kryo : ModelDBTests_00_PutGetDelete() {
    override fun testSerializer(): DefaultSerializer = KryoSerializer()
    override fun testTypeTable() = TypeTable.withFullName()

    class LDB : ModelDBTests_00_PutGetDelete_Kryo() { override val factory = ModelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : ModelDBTests_00_PutGetDelete_Kryo() { override val factory = ModelDB.inMemory }
}

abstract class ModelDBTests_01_Types_Kryo : ModelDBTests_01_Types() {
    override fun testSerializer(): DefaultSerializer = KryoSerializer()
    override fun testTypeTable() = TypeTable.withFullName()

    class LDB : ModelDBTests_01_Types_Kryo() { override val factory = ModelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : ModelDBTests_01_Types_Kryo() { override val factory = ModelDB.inMemory }
}

abstract class ModelDBTests_02_IDs_Kryo : ModelDBTests_02_IDs() {
    override fun testSerializer(): DefaultSerializer = KryoSerializer()
    override fun testTypeTable() = TypeTable.withFullName()

    class LDB : ModelDBTests_02_IDs_Kryo() { override val factory = ModelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : ModelDBTests_02_IDs_Kryo() { override val factory = ModelDB.inMemory }
}

abstract class ModelDBTests_03_Indexes_Kryo : ModelDBTests_03_Indexes() {
    override fun testSerializer(): DefaultSerializer = KryoSerializer()
    override fun testTypeTable() = TypeTable.withFullName()

    class LDB : ModelDBTests_03_Indexes_Kryo() { override val factory = ModelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : ModelDBTests_03_Indexes_Kryo() { override val factory = ModelDB.inMemory }
}

abstract class ModelDBTests_04_Refs_Kryo : ModelDBTests_04_Refs() {
    override fun testSerializer(): DefaultSerializer = KryoSerializer()
    override fun testTypeTable() = TypeTable.withFullName()

    class LDB : ModelDBTests_04_Refs_Kryo() { override val factory = ModelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : ModelDBTests_04_Refs_Kryo() { override val factory = ModelDB.inMemory }
}

abstract class ModelDBTests_05_Polymorphism_Kryo : ModelDBTests_05_PolymorphicTypeTable() {
    override fun testSerializer(): DefaultSerializer = KryoSerializer()
    override fun testTypeTable() = TypeTable.withFullName {
        root<Person>()
                .sub<Adult>()
                .sub<Child>()
    }

    class LDB : ModelDBTests_05_Polymorphism_Kryo() { override val factory = ModelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : ModelDBTests_05_Polymorphism_Kryo() { override val factory = ModelDB.inMemory }
}

abstract class ModelDBTests_06_All_Kryo : ModelDBTests_06_All() {
    override fun testSerializer(): DefaultSerializer = KryoSerializer()
    override fun testTypeTable() = TypeTable.withFullName()

    class LDB : ModelDBTests_06_All_Kryo() { override val factory = ModelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : ModelDBTests_06_All_Kryo() { override val factory = ModelDB.inMemory }
}

abstract class ModelDBTests_07_React_Kryo : ModelDBTests_07_React() {
    override fun testSerializer(): DefaultSerializer = KryoSerializer()
    override fun testTypeTable() = TypeTable.withFullName()

    class LDB : ModelDBTests_07_React_Kryo() { override val factory = ModelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : ModelDBTests_07_React_Kryo() { override val factory = ModelDB.inMemory }
}

abstract class ModelDBTests_09_Checks_Kryo : ModelDBTests_09_Checks() {
    override fun testSerializer(): DefaultSerializer = KryoSerializer()
    override fun testTypeTable() = TypeTable.withFullName()

    class LDB : ModelDBTests_09_Checks_Kryo() { override val factory = ModelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : ModelDBTests_09_Checks_Kryo() { override val factory = ModelDB.inMemory }
}
