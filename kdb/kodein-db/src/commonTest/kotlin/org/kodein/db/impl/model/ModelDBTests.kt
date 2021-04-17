package org.kodein.db.impl.model

import org.kodein.db.DBFactory
import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.db.inDir
import org.kodein.db.ldb.LevelDBOptions
import org.kodein.db.ldb.TrackClosableAllocation
import org.kodein.db.model.*
import org.kodein.db.model.orm.MetadataExtractor
import org.kodein.db.model.orm.DefaultSerializer
import org.kodein.db.orm.kotlinx.KotlinxSerializer
import org.kodein.memory.file.FileSystem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest


abstract class ModelDBTests {

    private var _mdb: ModelDB? = null

    protected val mdb: ModelDB get() = _mdb!!

    abstract val factory: DBFactory<ModelDB>

    open fun testSerializer(): DefaultSerializer? = KotlinxSerializer {
        +Adult.serializer()
        +Child.serializer()
        +Location.serializer()
        +City.serializer()
        +Birth.serializer()
    }

    open fun testClassSerializers(): List<DBClassSerializer<*>> = emptyList()

    open fun testMetadataExtractor(): MetadataExtractor? = null

    open fun testTypeTable(): TypeTable? = TypeTable()

    open fun newModelDB(): ModelDB {
        val options = ArrayList<Options.Open>()
        options.add(TrackClosableAllocation(true))
        testSerializer()?.let { options.add(it) }
        testMetadataExtractor()?.let { options.add(it) }
        testTypeTable()?.let { options.add(it) }
        testClassSerializers().forEach { options.add(it) }

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
