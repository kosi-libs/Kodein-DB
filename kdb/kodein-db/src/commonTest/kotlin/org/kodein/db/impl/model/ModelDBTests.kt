package org.kodein.db.impl.model

import org.kodein.db.*
import org.kodein.db.encryption.EncryptOptions
import org.kodein.db.encryption.Encryption
import org.kodein.db.inmemory.inMemory
import org.kodein.db.kv.TrackClosableAllocation
import org.kodein.db.model.*
import org.kodein.db.model.orm.MetadataExtractor
import org.kodein.db.model.orm.DefaultSerializer
import org.kodein.db.orm.kotlinx.KotlinxSerializer
import org.kodein.memory.file.FileSystem
import org.kodein.memory.io.Memory
import org.kodein.memory.text.array
import kotlin.test.*


interface ModelDBTestsBase {
    fun middlewares(): Array<out Middleware> = emptyArray()
    val factory: DBFactory<ModelDB>
}

abstract class ModelDBTests : ModelDBTestsBase {

    interface Encrypted : ModelDBTestsBase {
        override fun middlewares(): Array<out Middleware> {
            val encryptionKey = Memory.array("Encryption-Key")
            return arrayOf(Encryption(encryptionKey))
        }
    }

    interface LDB : ModelDBTestsBase {
        override val factory get() = ModelDB.default.inDir(FileSystem.tempDirectory.path)
    }

    interface IM : ModelDBTestsBase {
        override val factory get() = ModelDB.inMemory
    }


    private var _mdb: ModelDB? = null

    protected val mdb: ModelDB get() = _mdb!!

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
        options.addAll(middlewares())
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

    inner class CursorAssertBuilder<M : Any, C : ModelCursor<M>>(private val content: ArrayList<Pair<Key<*>, (C) -> Unit>>) {
        operator fun Key<M>.invoke(check: (C) -> Unit) {
            content.add(this to check)
        }

        fun K(model: M, check: (C) -> Unit) = mdb.keyFrom(model).invoke(check)
    }

    fun <M : Any, C : ModelCursor<M>> assertCursorIs(cursor: C, move: C.() -> Unit = ModelCursor<*>::next, contentBuild: CursorAssertBuilder<M, C>.() -> Unit) {
        val content = ArrayList<Pair<Key<*>, (C) -> Unit>>().also { CursorAssertBuilder(it).contentBuild() }

        if (this !is Encrypted) {
            content.forEach { (key, check) ->
                assertTrue(cursor.isValid())
                assertEquals(key, cursor.key())
                check(cursor)
                cursor.move()
            }
        } else {
            val contentMap = content.toMap()
            repeat(contentMap.size) {
                assertTrue(cursor.isValid())
                val check = contentMap[cursor.key()]
                assertNotNull(check)
                check(cursor)
                cursor.move()
            }
        }
        assertFalse(cursor.isValid())
    }
}
