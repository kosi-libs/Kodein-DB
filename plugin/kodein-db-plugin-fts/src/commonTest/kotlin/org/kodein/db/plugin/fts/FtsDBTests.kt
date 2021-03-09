package org.kodein.db.plugin.fts

import org.kodein.db.DBFactory
import org.kodein.db.model.ModelDB
import org.kodein.db.orm.kotlinx.KotlinxSerializer
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.byteArray
import org.kodein.db.test.utils.description
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.readAllBytes
import org.kodein.memory.io.startsWith
import org.kodein.memory.io.wrap
import org.kodein.memory.use
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.fail


abstract class FtsDBTests {

    private var _mdb: ModelDB? = null

    protected val mdb: ModelDB get() = _mdb!!

    abstract val factory: DBFactory<ModelDB>

    open fun newModelDB(): ModelDB {
        return factory.open("modeldb",
            KotlinxSerializer {
                +Contact.serializer()
            },
            FullTextSearch
        )
    }

    protected fun open() {
        _mdb = newModelDB()
    }

    @BeforeTest
    open fun setUp() {
        factory.destroy("ftsdb")
        open()
    }

    @AfterTest
    open fun tearDown() {
        _mdb?.close()
        _mdb = null
        factory.destroy("ftsdb")
    }

    fun assertFtsDBIs(vararg keyValues: Pair<ByteArray, ByteArray>) {
        mdb.data.ldb.newCursor().use { cursor ->
            val prefix = byteArray("Xfts")
            cursor.seekTo(KBuffer.wrap(prefix))
            var i = 0
            while (cursor.isValid() && cursor.transientKey().startsWith(prefix)) {
                if (i >= keyValues.size) {
                    fail("DB contains additional entrie(s): " + cursor.transientKey().readAllBytes().description())
                }
                assertBytesEquals(keyValues[i].first, cursor.transientKey(), prefix = "Key ${i + 1}: ")
                keyValues[i].second.let {
                    assertBytesEquals(it, cursor.transientValue(), prefix = "Value ${i + 1}: ")
                }
                cursor.next()
                i++
            }
            if (i < keyValues.size) {
                fail("DB is missing entrie(s):\n" + keyValues.takeLast(keyValues.size - i).joinToString("\n") { it.first.description() + ": " + it.second.description() })
            }
        }
    }
}
