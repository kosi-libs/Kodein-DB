package org.kodein.db.impl

import org.kodein.db.DB
import org.kodein.db.Options
import org.kodein.db.impl.model.*
import org.kodein.db.inDir
import org.kodein.db.model.DBSerializer
import org.kodein.db.orm.kotlinx.KotlinxSerializer
import org.kodein.db.test.utils.platformTmpPath
import kotlin.test.AfterTest
import kotlin.test.BeforeTest


abstract class DBTests {

    private var _db: DB? = null

    protected val db: DB get() = _db!!

    private val factory = DB.default.inDir(platformTmpPath)

    protected val kxSerializer = KotlinxSerializer {
        +Adult.serializer()
        +Child.serializer()
        +Location.serializer()
        +City.serializer()
        +Birth.serializer()
    }

    open fun options(): Array<out Options.Open> = arrayOf(DBSerializer(kxSerializer))

    protected fun open() {
        _db = factory.open("testdb", *options())
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

    protected object Models {
        val laila = Adult("Laila", "BRYS-ATIE", Date(25, 8, 1989))
        val salomon = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val sjeg = City("Saint Julien En Genevois", Location(46.1443, 6.0826), 74160)
        val paris = City("Paris", Location(48.864716, 2.349014), 75000)
        val pap = City("Pointe À Pitre", Location(16.2333, -61.5167), 97110)
    }

    protected fun inflateDB() {
        db.put(Models.paris)
        val sjeg = db.put(Models.sjeg)
        val pap = db.put(Models.pap)
        val salomon = db.put(Models.salomon)
        val laila = db.put(Models.laila)
        db.put(Birth(salomon, sjeg))
        db.put(Birth(laila, pap))
    }

}
