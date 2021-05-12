package org.kodein.db.impl.model

import org.kodein.db.*
import org.kodein.db.encryption.Encryption
import org.kodein.db.inmemory.inMemory
import org.kodein.db.model.ModelDB
import org.kodein.db.model.findAllByType
import org.kodein.db.model.get
import org.kodein.db.model.putAll
import org.kodein.memory.file.FileSystem
import org.kodein.memory.io.Memory
import org.kodein.memory.text.array
import org.kodein.memory.use
import kotlin.test.*

@Suppress("ClassName")
abstract class ModelDBTests_05_PolymorphicTypeTable : ModelDBTests() {

    class LDB : ModelDBTests_05_PolymorphicTypeTable(), ModelDBTests.LDB
    class IM : ModelDBTests_05_PolymorphicTypeTable(), ModelDBTests.IM

    abstract class Encrypted : ModelDBTests_05_PolymorphicTypeTable(), ModelDBTests.Encrypted {
        class LDB : Encrypted(), ModelDBTests.LDB
        class IM : Encrypted(), ModelDBTests.IM
    }


    override fun testTypeTable() = TypeTable {
        root<Person>()
                .sub<Adult>()
                .sub<Child>()
    }

    private val gilbert by lazy { Adult("Gilbert", "BRYS", Date(1, 9, 1954)) }
    private val veronique by lazy { Adult("Véronique", "BRYS", Date(17, 10, 1957)) }
    private val salomon by lazy { Child("Salomon", "BRYS", Date(15, 12, 1986), mdb.keyFrom(gilbert) to mdb.keyFrom(veronique)) }
    private val maroussia by lazy { Child("Maroussia", "BRYS", Date(18, 8, 1988), mdb.keyFrom(gilbert) to mdb.keyFrom(veronique)) }
    private val benjamin by lazy { Child("Benjamin", "BRYS", Date(23, 6, 1992), mdb.keyFrom(gilbert) to mdb.keyFrom(veronique)) }

    @Test
    fun test00_polymorphicKey() {
        val expectedKey = mdb.keyById<Adult>("BRYS", "Salomon")

        val polymorphicKey = mdb.keyById<Child>("BRYS", "Salomon") as Key<*>
        assertEquals(expectedKey, polymorphicKey)

        val modelKey = mdb.keyFrom(salomon) as Key<*>
        assertEquals(expectedKey, modelKey)
    }

    @Test
    fun test01_polymorphicPut() {
        mdb.putAll(listOf(gilbert, veronique, salomon))

        val me = mdb[mdb.keyById<Child>("BRYS", "Salomon")]
        assertNotNull(me)
        assertEquals(salomon, me.model)
    }

    @Test
    fun test02_PolymorphicCursor() {

        mdb.putAll(listOf(gilbert, veronique, salomon, maroussia, benjamin))

        mdb.findAllByType<Person>().use { cursor ->
            assertCursorIs(cursor) {
                K(benjamin) {
                    val benji = it.model()
                    assertEquals(benjamin, benji.model)
                    assertNotSame(benjamin, benji.model)
                }
                K(gilbert) {
                    val guilou = it.model()
                    assertEquals(gilbert, guilou.model)
                    assertNotSame(gilbert, guilou.model)
                }
                K(maroussia) {
                    val yaya = it.model()
                    assertEquals(maroussia, yaya.model)
                    assertNotSame(maroussia, yaya.model)
                }
                K(salomon) {
                    val monmon = it.model()
                    assertEquals(salomon, monmon.model)
                    assertNotSame(salomon, monmon.model)
                }
                K(veronique) {
                    val vero = it.model()
                    assertEquals(veronique, vero.model)
                    assertNotSame(veronique, vero.model)
                }
            }
        }
    }

    @Test
    fun test03_PolymorphicSubCursor() {

        mdb.putAll(listOf(gilbert, veronique, salomon, maroussia, benjamin))

        val ex = assertFailsWith<IllegalStateException> {
            mdb.findAllByType<Child>().use {}
        }
        assertEquals("Child is a sub type of Person. You must find by Person.", ex.message)
    }

}
