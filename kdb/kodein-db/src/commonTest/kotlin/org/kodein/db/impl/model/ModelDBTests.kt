package org.kodein.db.impl.model

import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.db.model.*
import org.kodein.db.orm.kotlinx.KotlinxSerializer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

expect object ModelDBTestFactory {
    fun destroy()
    fun open(vararg options: Options.Open): ModelDB
}


abstract class ModelDBTests {

    protected var _mdb: ModelDB? = null

    protected val mdb: ModelDB get() = _mdb!!

    open fun testSerializer(): Serializer<Any> = KotlinxSerializer {
        +Adult.serializer()
        +Child.serializer()
        +Location.serializer()
        +City.serializer()
        +Birth.serializer()
    }

    open fun testMetadataExtractor(): MetadataExtractor? = null

    open fun testTypeTable(): TypeTable? = null

    open fun newModelDB(): ModelDB {
        val options = ArrayList<Options.Open>()
        options.add(DBSerializer(testSerializer()))
        testMetadataExtractor()?.let { options.add(DBMetadataExtractor(it)) }
        testTypeTable()?.let { options.add(DBTypeTable(it)) }

        return ModelDBTestFactory.open(*options.toTypedArray())
    }

    @BeforeTest
    open fun setUp() {
        ModelDBTestFactory.destroy()
        _mdb = newModelDB()
    }

    @AfterTest
    open fun tearDown() {
        _mdb?.close()
        _mdb = null
        ModelDBTestFactory.destroy()
    }

}
