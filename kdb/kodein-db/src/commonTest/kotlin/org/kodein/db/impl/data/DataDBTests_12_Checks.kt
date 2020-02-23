package org.kodein.db.impl.data

import org.kodein.db.Anticipate
import org.kodein.db.Value
import org.kodein.memory.use
import org.kodein.memory.util.MaybeThrowable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@Suppress("ClassName")
class DataDBTests_12_Checks : DataDBTests() {

    @Test
    fun test00_putOK() {
        val key = ddb.newKey(1, Value.ofAscii("test"))

        ddb.put(key, Value.of(21))
        ddb.put(key, Value.of(42), emptySet(), Anticipate {
            ddb.get(key)!!.use {
                check(it.readInt() == 21)
            }
        })

        ddb.get(key)!!.use {
            assertEquals(42, it.readInt())
        }
    }

    @Test
    fun test01_putKO() {
        val key = ddb.newKey(1, Value.ofAscii("test"))

        ddb.put(key, Value.of(21))
        assertFailsWith<IllegalStateException> {
            ddb.put(key, Value.of(42), emptySet(), Anticipate {
                ddb.get(key)!!.use {
                    check(it.readInt() == 0)
                }
            })
        }

        ddb.get(key)!!.use {
            assertEquals(21, it.readInt())
        }
    }

    @Test
    fun test02_deleteOK() {
        val key = ddb.newKey(1, Value.ofAscii("test"))
        ddb.put(key, Value.of(42))

        ddb.delete(key, Anticipate {
            ddb.get(key)!!.use {
                check(it.readInt() == 42)
            }
        })

        assertNull(ddb.get(key))
    }

    @Test
    fun test03_deleteKO() {
        val key = ddb.newKey(1, Value.ofAscii("test"))
        ddb.put(key, Value.of(42))

        assertFailsWith<IllegalStateException> {
            ddb.delete(key, Anticipate {
                ddb.get(ddb.newKey(1, Value.ofAscii("test")))!!.use {
                    check(it.readInt() == 0)
                }
            })
        }

        ddb.get(key)!!.use {
            assertEquals(42, it.readInt())
        }
    }

    @Test
    fun test04_batchOK() {
        val key = ddb.newKey(1, Value.ofAscii("test"))
        ddb.put(key, Value.of(21))

        ddb.newBatch().use { batch ->
            batch.put(key, Value.of(42))
            MaybeThrowable().also {
                batch.write(it, Anticipate {
                    ddb.get(key)!!.use {
                        check(it.readInt() == 21)
                    }
                })
            }.shoot()
        }

        ddb.get(key)!!.use {
            assertEquals(42, it.readInt())
        }
    }

    @Test
    fun test05_batchKO() {
        val key = ddb.newKey(1, Value.ofAscii("test"))
        ddb.put(key, Value.of(21))

        ddb.newBatch().use { batch ->
            batch.put(key, Value.of(42))
            assertFailsWith<IllegalStateException> {
                MaybeThrowable().also {
                    batch.write(it, Anticipate {
                        ddb.get(key)!!.use {
                            check(it.readInt() == 0)
                        }
                    })
                }.shoot()
            }
        }

        ddb.get(key)!!.use {
            assertEquals(21, it.readInt())
        }
    }

}
