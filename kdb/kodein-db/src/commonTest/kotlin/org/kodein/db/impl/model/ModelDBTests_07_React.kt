package org.kodein.db.impl.model

import org.kodein.db.*
import org.kodein.db.encryption.Encryption
import org.kodein.db.inmemory.inMemory
import org.kodein.db.model.ModelDB
import org.kodein.db.model.delete
import org.kodein.db.model.get
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.Closeable
import org.kodein.memory.file.FileSystem
import org.kodein.memory.io.Memory
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.text.Charset
import org.kodein.memory.text.array
import org.kodein.memory.text.readString
import org.kodein.memory.use
import org.kodein.memory.util.MaybeThrowable
import kotlin.test.*

@Suppress("ClassName")
abstract class ModelDBTests_07_React : ModelDBTests() {

    class LDB : ModelDBTests_07_React(), ModelDBTests.LDB
    class IM : ModelDBTests_07_React(), ModelDBTests.IM

    abstract class Encrypted : ModelDBTests_07_React(), ModelDBTests.Encrypted {
        class LDB : Encrypted(), ModelDBTests.LDB
        class IM : Encrypted(), ModelDBTests.IM
    }


    @Test
    fun test00_PutDelete() {

        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))

        var setSubscriptionCalls = 0
        var willPutCalls = 0
        var didPutCalls = 0
        var willDeleteCalls = 0
        var didDeleteCalls = 0

        val listener = object : DBListener<Any> {
            override fun setSubscription(subscription: Closeable) {
                ++setSubscriptionCalls
            }

            override fun willPut(model: Any, typeName: ReadMemory, metadata: Metadata, options: Array<out Options.Puts>) {
                assertSame(me, model)
                assertEquals("Adult", typeName.readString().split(".").last())
                assertEquals(me.id, metadata.id)
                assertEquals(me.indexes(), metadata.indexes())
                ++willPutCalls
            }

            override fun didPut(model: Any, key: Key<*>, typeName: ReadMemory, metadata: Metadata, size: Int, options: Array<out Options.Puts>) {
                assertSame(me, model)
                assertEquals("Adult", typeName.readString().split(".").last())
                assertEquals(me.id, metadata.id)
                assertEquals(me.indexes(), metadata.indexes())
                ++didPutCalls
            }

            override fun willDelete(key: Key<*>, getModel: () -> Any?, typeName: ReadMemory, options: Array<out Options.Deletes>) {
                assertEquals(mdb.keyFrom(me), key)
                assertEquals("Adult", typeName.readString().split(".").last())
                val model = getModel()
                assertNotSame(me, model)
                assertEquals(me, model)
                assertSame(model, getModel())
                ++willDeleteCalls
            }

            override fun didDelete(key: Key<*>, model: Any?, typeName: ReadMemory, options: Array<out Options.Deletes>) {
                assertEquals(mdb.keyFrom(me), key)
                assertNotSame(me, model)
                assertEquals(me, model)
                assertEquals("Adult", typeName.readString().split(".").last())
                ++didDeleteCalls
            }
        }

        mdb.register(listener)
        mdb.register(listener)

        assertEquals(1, setSubscriptionCalls)
        assertEquals(0, willPutCalls)
        assertEquals(0, didPutCalls)
        assertEquals(0, willDeleteCalls)
        assertEquals(0, didDeleteCalls)

        mdb.put(me)

        assertEquals(1, setSubscriptionCalls)
        assertEquals(1, willPutCalls)
        assertEquals(1, didPutCalls)
        assertEquals(0, willDeleteCalls)
        assertEquals(0, didDeleteCalls)

        mdb.delete(mdb.keyFrom(me))

        assertEquals(1, setSubscriptionCalls)
        assertEquals(1, willPutCalls)
        assertEquals(1, didPutCalls)
        assertEquals(1, willDeleteCalls)
        assertEquals(1, didDeleteCalls)
    }

    @Test
    fun test01_Batch() {

        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val her = Adult("Laila", "ATIE", Date(25, 8, 1989))

        var setSubscriptionCalls = 0
        var willPutCalls = 0
        var didPutCalls = 0
        var willDeleteCalls = 0
        var didDeleteCalls = 0

        val listener = object : DBListener<Any> {
            override fun setSubscription(subscription: Closeable) {
                ++setSubscriptionCalls
            }

            override fun willPut(model: Any, typeName: ReadMemory, metadata: Metadata, options: Array<out Options.Puts>) {
                if (willPutCalls == 0)
                    assertSame(me, model)
                else
                    assertSame(her, model)
                ++willPutCalls
            }

            override fun didPut(model: Any, key: Key<*>, typeName: ReadMemory, metadata: Metadata, size: Int, options: Array<out Options.Puts>) {
                if (didPutCalls == 0)
                    assertSame(me, model)
                else
                    assertSame(her, model)
                ++didPutCalls
            }

            override fun willDelete(key: Key<*>, getModel: () -> Any?, typeName: ReadMemory, options: Array<out Options.Deletes>) {
                if (willDeleteCalls == 0)
                    assertEquals(me, getModel())
                else
                    assertEquals(her, getModel())
                ++willDeleteCalls
            }

            override fun didDelete(key: Key<*>, model: Any?, typeName: ReadMemory, options: Array<out Options.Deletes>) {
                if (didDeleteCalls == 0) {
                    assertEquals(mdb.keyFrom(me), key)
                    assertEquals(me, model)
                } else {
                    assertEquals(mdb.keyFrom(her), key)
                    assertEquals(her, model)
                }
                ++didDeleteCalls
            }
        }

        mdb.register(listener)

        assertEquals(1, setSubscriptionCalls)
        assertEquals(0, willPutCalls)
        assertEquals(0, didPutCalls)
        assertEquals(0, willDeleteCalls)
        assertEquals(0, didDeleteCalls)

        lateinit var meKey: Key<Adult>
        lateinit var herKey: Key<Adult>

        mdb.newBatch().use { batch ->
            meKey = batch.keyFrom(me)
            herKey = batch.keyFrom(her)
            batch.put(meKey, me)
            batch.put(herKey, her)

            assertEquals(1, setSubscriptionCalls)
            assertEquals(2, willPutCalls)
            assertEquals(0, didPutCalls)
            assertEquals(0, willDeleteCalls)
            assertEquals(0, didDeleteCalls)

            MaybeThrowable().also { batch.write(it) }.shoot()
        }

        assertEquals(1, setSubscriptionCalls)
        assertEquals(2, willPutCalls)
        assertEquals(2, didPutCalls)
        assertEquals(0, willDeleteCalls)
        assertEquals(0, didDeleteCalls)

        mdb.newBatch().use { batch ->
            batch.delete(meKey)
            batch.delete(herKey)

            assertEquals(1, setSubscriptionCalls)
            assertEquals(2, willPutCalls)
            assertEquals(2, didPutCalls)
            assertEquals(2, willDeleteCalls)
            assertEquals(0, didDeleteCalls)

            MaybeThrowable().also { batch.write(it) }.shoot()
        }

        assertEquals(1, setSubscriptionCalls)
        assertEquals(2, willPutCalls)
        assertEquals(2, didPutCalls)
        assertEquals(2, willDeleteCalls)
        assertEquals(2, didDeleteCalls)
    }

    @Test
    fun test02_SubscriptionClosed() {

        var willPutCalled = false
        var willDeleteCalled = false

        val putListener = object : DBListener<Any> {
            private lateinit var sub: Closeable
            override fun setSubscription(subscription: Closeable) { sub = subscription }
            override fun willPut(model: Any, typeName: ReadMemory, metadata: Metadata, options: Array<out Options.Puts>) {
                sub.close()
                willPutCalled = true
            }
            override fun didPut(model: Any, key: Key<*>, typeName: ReadMemory, metadata: Metadata, size: Int, options: Array<out Options.Puts>) = fail("didPut")
            override fun willDelete(key: Key<*>, getModel: () -> Any?, typeName: ReadMemory, options: Array<out Options.Deletes>) = fail("willDeltete")
            override fun didDelete(key: Key<*>, model: Any?, typeName: ReadMemory, options: Array<out Options.Deletes>) = fail("didDelete")
        }

        val deleteListener = object : DBListener<Any> {
            private lateinit var sub: Closeable
            override fun setSubscription(subscription: Closeable) { sub = subscription }
            override fun willDelete(key: Key<*>, getModel: () -> Any?, typeName: ReadMemory, options: Array<out Options.Deletes>) {
                sub.close()
                willDeleteCalled = true
            }
            override fun didDelete(key: Key<*>, model: Any?, typeName: ReadMemory, options: Array<out Options.Deletes>) = fail("didDelete")
        }

        mdb.register(putListener)
        mdb.register(deleteListener)

        assertFalse(willPutCalled)
        assertFalse(willDeleteCalled)

        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val key = mdb.keyFrom(me)
        mdb.put(key, me)

        assertTrue(willPutCalled)
        assertFalse(willDeleteCalled)

        mdb.delete(key)

        assertTrue(willDeleteCalled)
    }

    @Test
    fun test03_WillOpExceptions() {

        var firstWillPutCalled = false
        var firstDidPutCalled = false
        var secondDidPutCalled = false
        var thirdWillPutCalled = false
        var thirdDidPutCalled = false

        var firstWillDeleteCalled = false
        var firstDidDeleteCalled = false
        var secondDidDeleteCalled = false
        var thirdWillDeleteCalled = false
        var thirdDidDeleteCalled = false

        val firstListener = object : DBListener<Any> {
            override fun willPut(model: Any, typeName: ReadMemory, metadata: Metadata, options: Array<out Options.Puts>) { firstWillPutCalled = true }
            override fun didPut(model: Any, key: Key<*>, typeName: ReadMemory, metadata: Metadata, size: Int, options: Array<out Options.Puts>) { firstDidPutCalled = true }
            override fun willDelete(key: Key<*>, getModel: () -> Any?, typeName: ReadMemory, options: Array<out Options.Deletes>) { firstWillDeleteCalled = true }
            override fun didDelete(key: Key<*>, model: Any?, typeName: ReadMemory, options: Array<out Options.Deletes>) { firstDidDeleteCalled = true }
        }

        val secondListener = object : DBListener<Any> {
            override fun willPut(model: Any, typeName: ReadMemory, metadata: Metadata, options: Array<out Options.Puts>) = throw IllegalStateException("willPut")
            override fun didPut(model: Any, key: Key<*>, typeName: ReadMemory, metadata: Metadata, size: Int, options: Array<out Options.Puts>) { secondDidPutCalled = true }
            override fun willDelete(key: Key<*>, getModel: () -> Any?, typeName: ReadMemory, options: Array<out Options.Deletes>) = throw IllegalStateException("willDelete")
            override fun didDelete(key: Key<*>, model: Any?, typeName: ReadMemory, options: Array<out Options.Deletes>) { secondDidDeleteCalled = true }
        }

        val thirdListener = object : DBListener<Any> {
            override fun willPut(model: Any, typeName: ReadMemory, metadata: Metadata, options: Array<out Options.Puts>) { thirdWillPutCalled = true }
            override fun didPut(model: Any, key: Key<*>, typeName: ReadMemory, metadata: Metadata, size: Int, options: Array<out Options.Puts>) { thirdDidPutCalled = true }
            override fun willDelete(key: Key<*>, getModel: () -> Any?, typeName: ReadMemory, options: Array<out Options.Deletes>) { thirdWillDeleteCalled = true }
            override fun didDelete(key: Key<*>, model: Any?, typeName: ReadMemory, options: Array<out Options.Deletes>) { thirdDidDeleteCalled = true }
        }

        mdb.register(firstListener)
        mdb.register(secondListener)
        mdb.register(thirdListener)

        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val key = mdb.keyFrom(me)

        val putEx = assertFailsWith<IllegalStateException>("willPut") {
            mdb.put(me)
        }
        assertNull(putEx.cause)
        assertEquals(emptyList(), putEx.suppressedExceptions)

        assertNull(mdb[key])

        assertTrue(firstWillPutCalled)
        assertFalse(firstDidPutCalled)
        assertFalse(secondDidPutCalled)
        assertFalse(thirdWillPutCalled)
        assertFalse(thirdDidPutCalled)

        val deleteEx = assertFailsWith<IllegalStateException>("willDelete") {
            mdb.delete(key)
        }
        assertNull(deleteEx.cause)
        assertEquals(emptyList(), deleteEx.suppressedExceptions)

        assertTrue(firstWillDeleteCalled)
        assertFalse(firstDidDeleteCalled)
        assertFalse(secondDidDeleteCalled)
        assertFalse(thirdWillDeleteCalled)
        assertFalse(thirdDidDeleteCalled)
    }

    @Test
    fun test03_DidOpExceptions() {

        val firstListener = object : DBListener<Any> {
            override fun didPut(model: Any, key: Key<*>, typeName: ReadMemory, metadata: Metadata, size: Int, options: Array<out Options.Puts>) = throw IllegalStateException("first didPut")
            override fun didDelete(key: Key<*>, model: Any?, typeName: ReadMemory, options: Array<out Options.Deletes>) = throw IllegalStateException("first didDelete")
        }

        val secondListener = object : DBListener<Any> {
            override fun didPut(model: Any, key: Key<*>, typeName: ReadMemory, metadata: Metadata, size: Int, options: Array<out Options.Puts>) = throw IllegalStateException("second didPut")
            override fun didDelete(key: Key<*>, model: Any?, typeName: ReadMemory, options: Array<out Options.Deletes>) = throw IllegalStateException("second didDelete")
        }

        mdb.register(firstListener)
        mdb.register(secondListener)

        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val key = mdb.keyFrom(me)

        val putEx = assertFailsWith<IllegalStateException>("first didPut") {
            mdb.put(me)
        }
        assertNull(putEx.cause)
        assertEquals(1, putEx.suppressedExceptions.size)
        assertEquals("second didPut", (putEx.suppressedExceptions[0] as IllegalStateException).message)

        assertNotNull(mdb[key])

        val deleteEx = assertFailsWith<IllegalStateException>("first willDelete") {
            mdb.delete(key)
        }
        assertNull(deleteEx.cause)
        assertEquals(1, deleteEx.suppressedExceptions.size)
        assertEquals("second didDelete", (deleteEx.suppressedExceptions[0] as IllegalStateException).message)

        assertNull(mdb[key])
    }

    @Test
    fun test04_LazyModel() {
        var passed = false

        mdb.register(object : DBListener<Any> {
            override fun didDelete(key: Key<*>, model: Any?, typeName: ReadMemory, options: Array<out Options.Deletes>) {
                assertNull(model)
                passed = true
            }
        })

        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))

        mdb.put(me)
        mdb.delete(mdb.keyFrom(me))

        assertTrue(passed)
    }

}
