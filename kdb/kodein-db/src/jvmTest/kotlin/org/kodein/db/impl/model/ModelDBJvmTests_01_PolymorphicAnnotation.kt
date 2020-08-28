package org.kodein.db.impl.model

import org.kodein.db.TypeTable
import org.kodein.db.model.findAllByType
import org.kodein.db.model.orm.DefaultSerializer
import org.kodein.db.model.putAll
import org.kodein.db.orm.kryo.KryoSerializer
import org.kodein.memory.use
import kotlin.test.*

@Suppress("ClassName")
class ModelDBJvmTests_01_PolymorphicAnnotation : ModelDBTests() {

    override fun testSerializer(): DefaultSerializer = KryoSerializer()

    override fun testTypeTable(): TypeTable? = null

    @Test
    fun test01_PolymorphicAnnotations() {
        val gilbert = AAdult(0, "Gilbert", "BRYS", Date(1, 9, 1954))
        val veronique = AAdult(1, "Véronique", "BRYS", Date(17, 10, 1957))
        val salomon = AChild(2, "Salomon", "BRYS", Date(15, 12, 1986), mdb.keyFrom(gilbert) to mdb.keyFrom(veronique))
        val maroussia = AChild(3, "Maroussia", "BRYS", Date(18, 8, 1988), mdb.keyFrom(gilbert) to mdb.keyFrom(veronique))
        val benjamin = AChild(4, "Benjamin", "BRYS", Date(23, 6, 1992), mdb.keyFrom(gilbert) to mdb.keyFrom(veronique))

        mdb.putAll(listOf(gilbert, veronique, salomon, maroussia, benjamin))

        mdb.findAllByType<APerson>().use {
            assertTrue(it.isValid())
            val guilou = it.model()
            assertEquals(gilbert, guilou.model)

            it.next()
            assertTrue(it.isValid())
            val vero = it.model()
            assertEquals(veronique, vero.model)

            it.next()
            assertTrue(it.isValid())
            val monmon = it.model()
            assertEquals(salomon, monmon.model)

            it.next()
            assertTrue(it.isValid())
            val yaya = it.model()
            assertEquals(maroussia, yaya.model)

            it.next()
            assertTrue(it.isValid())
            val benji = it.model()
            assertEquals(benjamin, benji.model)

            it.next()
            assertFalse(it.isValid())
        }
    }

}
