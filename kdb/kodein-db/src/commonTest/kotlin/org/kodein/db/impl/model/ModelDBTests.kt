package org.kodein.db.impl.model

import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.db.inDir
import org.kodein.db.model.*
import org.kodein.db.model.orm.MetadataExtractor
import org.kodein.db.model.orm.Serializer
import org.kodein.db.orm.kotlinx.KotlinxSerializer
import org.kodein.db.test.utils.platformTmpPath
import kotlin.test.AfterTest
import kotlin.test.BeforeTest


abstract class ModelDBTests {

    private var _mdb: ModelDB? = null

    protected val mdb: ModelDB get() = _mdb!!

    private val factory = ModelDB.default.inDir(platformTmpPath)

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

        return factory.open("modeldb", *options.toTypedArray())
    }

    protected fun open() {
        _mdb = newModelDB()
    }

    @BeforeTest
    open fun setUp() {
        factory.destroy("modeldb")
        open()
    }

    @AfterTest
    open fun tearDown() {
        _mdb?.close()
        _mdb = null
        factory.destroy("modeldb")
    }

}
