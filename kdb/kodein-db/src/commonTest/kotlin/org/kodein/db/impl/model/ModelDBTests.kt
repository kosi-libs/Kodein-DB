package org.kodein.db.impl.model

import org.kodein.db.TypeTable
import org.kodein.db.model.MetadataExtractor
import org.kodein.db.model.ModelDB
import org.kodein.db.model.Serializer
import org.kodein.db.orm.kotlinx.KotlinxSerializer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

expect object ModelDBTestFactory {
    fun destroy()
    fun open(options: ModelDB.OpenOptions): ModelDB
}


abstract class ModelDBTests {

    protected var _mdb: ModelDB? = null

    protected val mdb: ModelDB get() = _mdb!!

    open fun testSerializer(): Serializer = KotlinxSerializer {
        +Adult.serializer()
        +Child.serializer()
        +Location.serializer()
        +City.serializer()
        +Birth.serializer()
    }

    open fun testMetadataExtractor(): MetadataExtractor? = null

    open fun testTypeTable(): TypeTable? = null

    open fun newModelDB(): ModelDB = ModelDBTestFactory.open(
            ModelDB.OpenOptions(
                    testSerializer(),
                    testMetadataExtractor(),
                    testTypeTable()
            )
    )

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
