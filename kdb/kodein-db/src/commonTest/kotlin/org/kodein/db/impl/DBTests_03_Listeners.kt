package org.kodein.db.impl

import org.kodein.db.delete
import org.kodein.db.execBatch
import org.kodein.db.impl.model.Adult
import org.kodein.db.impl.model.Person
import org.kodein.db.on
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.memory.text.toAsciiBytes
import kotlin.test.*

@Suppress("ClassName")
open class DBTests_03_Listeners : DBTests() {

    @Test
    fun test00_put() {
        db.on<Person>().register {
            var wCounter = 0
            var dCounter = 0
            willPut {
                assertBytesEquals("Adult".toAsciiBytes(), typeName)
                when (wCounter++) {
                    0 -> {
                        assertEquals(0, dCounter)
                        assertSame(Models.salomon, it)
                    }
                    1 -> {
                        assertEquals(1, dCounter)
                        assertSame(Models.laila, it)
                    }
                    else -> fail()
                }
            }
            willDelete { fail() }
            didPut {
                assertBytesEquals("Adult".toAsciiBytes(), typeName)
                when (dCounter++) {
                    0 -> {
                        assertEquals(1, wCounter)
                        assertSame(Models.salomon, it)
                    }
                    1 -> {
                        assertEquals(2, wCounter)
                        assertSame(Models.laila, it)
                    }
                    else -> fail()
                }
            }
            didDelete { fail() }
        }

        db.inflateDB()
    }

    @Test
    fun test01_delete() {
        db.inflateDB()

        db.on<Person>().register {
            var wCounter = 0
            var dCounter = 0
            willPut { fail() }
            willDelete {
                assertBytesEquals("Adult".toAsciiBytes(), typeName)
                when (wCounter++) {
                    0 -> {
                        assertEquals(0, dCounter)
                    }
                    1 -> {
                        assertEquals(1, dCounter)
                    }
                    else -> fail()
                }
            }
            didPut { fail() }
            didDelete {
                assertBytesEquals("Adult".toAsciiBytes(), typeName)
                when (dCounter++) {
                    0 -> {
                        assertEquals(1, wCounter)
                    }
                    1 -> {
                        assertEquals(2, wCounter)
                    }
                    else -> fail()
                }
            }
        }

        db.delete(db.keyFrom(Models.salomon))
        db.delete(db.keyFrom(Models.laila))
    }

    @Test
    fun test02_deleteIt() {
        db.inflateDB()

        db.on<Person>().register {
            var wCounter = 0
            var dCounter = 0
            willPut { fail() }
            willDeleteIt {
                assertBytesEquals("Adult".toAsciiBytes(), typeName)
                when (wCounter++) {
                    0 -> {
                        assertEquals(0, dCounter)
                        assertEquals(Models.salomon, it)
                    }
                    1 -> {
                        assertEquals(1, dCounter)
                        assertEquals(Models.laila, it)
                    }
                    else -> fail()
                }
            }
            didPut { fail() }
            didDeleteIt {
                assertBytesEquals("Adult".toAsciiBytes(), typeName)
                when (dCounter++) {
                    0 -> {
                        assertEquals(1, wCounter)
                        assertEquals(Models.salomon, it)
                    }
                    1 -> {
                        assertEquals(2, wCounter)
                        assertEquals(Models.laila, it)
                    }
                    else -> fail()
                }
            }
        }

        db.delete(db.keyFrom(Models.salomon))
        db.delete(db.keyFrom(Models.laila))
    }

    @Test
    fun test03_Batch() {
        var pBatchApplied = false
        var dBatchApplied = false

        db.on<Person>().register {
            var wCounter = 0
            var dCounter = 0
            willPut {
                assertBytesEquals("Adult".toAsciiBytes(), typeName)
                when (wCounter++) {
                    0 -> {
                        assertEquals(0, dCounter)
                        assertSame(Models.salomon, it)
                    }
                    1 -> {
                        assertEquals(0, dCounter)
                        assertSame(Models.laila, it)
                    }
                    else -> fail()
                }
            }
            willDeleteIt {
                assertBytesEquals("Adult".toAsciiBytes(), typeName)
                when (wCounter++) {
                    2 -> {
                        assertEquals(2, dCounter)
                        assertEquals(Models.salomon, it)
                    }
                    3 -> {
                        assertEquals(2, dCounter)
                        assertEquals(Models.laila, it)
                    }
                    else -> fail()
                }
            }
            didPut {
                assertTrue(pBatchApplied)
                assertBytesEquals("Adult".toAsciiBytes(), typeName)
                when (dCounter++) {
                    0 -> {
                        assertEquals(2, wCounter)
                        assertSame(Models.salomon, it)
                    }
                    1 -> {
                        assertEquals(2, wCounter)
                        assertSame(Models.laila, it)
                    }
                    else -> fail()
                }
            }
            didDeleteIt {
                assertTrue(dBatchApplied)
                assertBytesEquals("Adult".toAsciiBytes(), typeName)
                when (dCounter++) {
                    2 -> {
                        assertEquals(4, wCounter)
                        assertEquals(Models.salomon, it)
                    }
                    3 -> {
                        assertEquals(4, wCounter)
                        assertEquals(Models.laila, it)
                    }
                    else -> fail()
                }
            }
        }

        db.execBatch {
            inflateModels()
            pBatchApplied = true
        }

        db.execBatch {
            delete(keyFrom(Models.salomon))
            delete(keyFrom(Models.laila))
            dBatchApplied = true
        }
    }

    @Test
    fun test03_unsubscribeWillPut() {
        db.on<Adult>().register {
            willPut { subscription.close() }
            didPut { fail() }
            willDelete { fail() }
            didDelete { fail() }
        }

        val key = db.put(Models.salomon)
        db.delete(key)
    }

    @Test
    fun test04_unsubscribeDidPut() {
        db.on<Adult>().register {
            didPut { subscription.close() }
            willDelete { fail() }
            didDelete { fail() }
        }

        val key = db.put(Models.salomon)
        db.delete(key)
    }

    @Test
    fun test05_unsubscribeWillDelete() {
        db.on<Adult>().register {
            willDelete { subscription.close() }
            didDelete { fail() }
        }

        val key = db.put(Models.salomon)
        db.delete(key)
    }

    @Test
    fun test06_unsubscribeDidDelete() {
        db.on<Adult>().register {
            var counter = 0
            willPut { assertEquals(0, counter) }
            didDelete {
                ++counter
                subscription.close()
            }
        }

        val key = db.put(Models.salomon)
        db.delete(key)
        db.put(Models.salomon)
    }
}
