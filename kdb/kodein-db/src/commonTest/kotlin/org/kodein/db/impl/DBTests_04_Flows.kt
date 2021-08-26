package org.kodein.db.impl

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.kodein.db.Operation
import org.kodein.db.deleteFrom
import org.kodein.db.impl.model.Adult
import org.kodein.db.impl.model.Date
import org.kodein.db.flowOf
import org.kodein.db.keyById
import org.kodein.db.on
import org.kodein.db.stateFlowOfId
import org.kodein.db.test.utils.runBlockingTest
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
abstract class DBTests_04_Flows : DBTests() {

    class LDB : DBTests_04_Flows(), DBTests.LDB
    class IM : DBTests_04_Flows(), DBTests.IM

    abstract class Encrypted : DBTests_04_Flows(), DBTests.Encrypted {
        class LDB : Encrypted(), DBTests.LDB
        class IM : Encrypted(), DBTests.IM
    }

    @Test
    fun test00_operationFlow() {
        runBlockingTest {
            val flow = db.on<Adult>().operationFlow()

            var expected = ""
            var count = 0
            launch {
                flow.collect {
                    ++count
                    val str = when (it) {
                        is Operation.Put -> "Put:${it.model}"
                        is Operation.Delete -> "Delete:${it.model()}"
                    }
                    assertEquals(expected, str)
                }
            }

            repeat(10) { yield() }

            var salomon = Adult("BRYS", "Salomon", Date(15, 12, 1786))
            db.put(salomon)

            expected = "Put:Adult(firstName=BRYS, lastName=Salomon, birth=Date(day=15, month=12, year=1786))"
            assertEquals(0, count)
            repeat(10) { yield() }
            assertEquals(1, count)

            salomon = salomon.copy(birth = salomon.birth.copy(year = 1986))
            db.put(salomon)

            expected = "Put:Adult(firstName=BRYS, lastName=Salomon, birth=Date(day=15, month=12, year=1986))"
            assertEquals(1, count)
            repeat(10) { yield() }
            assertEquals(2, count)

            db.deleteFrom(salomon)

            expected = "Delete:Adult(firstName=BRYS, lastName=Salomon, birth=Date(day=15, month=12, year=1986))"
            assertEquals(2, count)
            repeat(10) { yield() }
            assertEquals(3, count)
        }
    }

    @Test
    fun test01_stateFlow() {
        runBlockingTest {
            val flow = db.stateFlowOfId<Adult>(this, "Salomon", "BRYS")

            var expected: Adult? = null
            var count = 0
            launch {
                flow.collect {
                    ++count
                    assertEquals(expected, it)
                }
            }

            assertEquals(0, count)
            repeat(10) { yield() }
            assertEquals(1, count)

            val salomon = Adult("BRYS", "Salomon", Date(15, 12, 1986))
            db.put(salomon)

            expected = salomon
            assertEquals(1, count)
            repeat(10) { yield() }
            assertEquals(2, count)

            db.deleteFrom(salomon)

            expected = null
            assertEquals(2, count)
            repeat(10) { yield() }
            assertEquals(3, count)
        }
    }

    @Test
    fun test02_flowOf() {
        runBlockingTest {
            val flow = db.flowOf<Adult>(db.keyById("Salomon", "BRYS"))

            var expected: Adult? = null
            var count = 0
            launch {
                flow.collect {
                    ++count
                    assertEquals(expected, it)
                }
            }

            assertEquals(0, count)
            repeat(10) { yield() }
            assertEquals(1, count)

            val salomon = Adult("BRYS", "Salomon", Date(15, 12, 1986))
            db.put(salomon)

            expected = salomon
            assertEquals(1, count)
            repeat(10) { yield() }
            assertEquals(2, count)

            // We put someone into the database who doesn't match the key given to flowOf and make sure there is no flow
            // emissions.
            val imposter = Adult("BRYS", "Solomon", Date(15, 12, 1986))
            db.put(imposter)

            assertEquals(2, count)
            repeat(10) { yield() }
            assertEquals(2, count)

            // Deleting Salomon should emit because we are observing him.
            db.deleteFrom(salomon)

            expected = null
            assertEquals(2, count)
            repeat(10) { yield() }
            assertEquals(3, count)

            // Deleting the imposter shouldn't emit since we are only observing Salomon.
            db.deleteFrom(imposter)

            assertEquals(3, count)
            repeat(10) { yield() }
            assertEquals(3, count)
        }
    }

}
