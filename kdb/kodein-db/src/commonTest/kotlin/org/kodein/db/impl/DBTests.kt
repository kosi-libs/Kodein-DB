package org.kodein.db.impl

import org.kodein.db.*
import org.kodein.db.encryption.Encryption
import org.kodein.db.impl.model.*
import org.kodein.db.inmemory.inMemory
import org.kodein.db.kv.TrackClosableAllocation
import org.kodein.db.model.cache.ModelCache
import org.kodein.db.orm.kotlinx.KotlinxSerializer
import org.kodein.memory.file.FileSystem
import org.kodein.memory.io.Memory
import org.kodein.memory.text.array
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals


interface DBTestsBase {
    fun middlewares(): Array<out Middleware> = emptyArray()
    val factory: DBFactory<DB>
    fun baseOptions(): Array<out Options.Open> = emptyArray()
}

abstract class DBTests : DBTestsBase {

    interface Encrypted : DBTestsBase {
        override fun middlewares(): Array<out Middleware> = arrayOf(Encryption(Memory.array("Encryption-Key")))
    }

    interface LDB : DBTestsBase {
        override val factory get() = DB.inDir(FileSystem.tempDirectory.path)
    }

    interface IM : DBTestsBase {
        override val factory get() = DB.inMemory
    }

    interface NoCache : DBTestsBase {
        override fun baseOptions(): Array<out Options.Open> = arrayOf(ModelCache.Disable)
    }

    private var _db: DB? = null
    protected val db: DB get() = _db!!

    val kxSerializer = KotlinxSerializer {
        +Adult.serializer()
        +Child.serializer()
        +Location.serializer()
        +City.serializer()
        +Birth.serializer()
    }

    open fun testOptions(): Array<out Options.Open> = arrayOf(kxSerializer, TypeTable(), TrackClosableAllocation(true))

    private fun open() {
        val options = middlewares().asList() + baseOptions().asList() + testOptions().asList()
        _db = factory.open("testdb", *options.toTypedArray())
    }

    @BeforeTest
    open fun setUp() {
        factory.destroy("testdb")
        open()
    }

    @AfterTest
    open fun tearDown() {
        _db?.close()
        _db = null
        factory.destroy("testdb")
    }

    object Models {
        val laila = Adult("Laila", "BRYS-ATIE", Date(25, 8, 1989))
        val salomon = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val sjeg = City("Saint Julien En Genevois", Location(46.1443, 6.0826), 74160)
        val paris = City("Paris", Location(48.864716, 2.349014), 75000)
        val pap = City("Pointe À Pitre", Location(16.2333, -61.5167), 97110)

        fun salomonBirth(db: DB) = Birth(db.keyFrom(salomon), db.keyFrom(sjeg))
        fun lailaBirth(db: DB) = Birth(db.keyFrom(laila), db.keyFrom(pap))
    }

    fun DBWrite.inflateModels() {
        put(Models.paris)
        put(Models.sjeg)
        put(Models.pap)
        put(Models.salomon)
        put(Models.laila)
    }

    fun DBWrite.inflateDB() {
        inflateModels()
        put(Models.salomonBirth(db))
        put(Models.lailaBirth(db))
    }

    fun <T> assertListEquals(expected: List<T>, actual: List<T>) {
        if (this !is Encrypted) assertEquals(expected, actual)
        else assertEquals(expected.toSet(), actual.toSet())
    }

}
