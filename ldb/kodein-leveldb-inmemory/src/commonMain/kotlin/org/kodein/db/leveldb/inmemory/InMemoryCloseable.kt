//package org.kodein.db.leveldb.inmemory
//
//import org.kodein.memory.Closeable
//
//public abstract class InMemoryCloseable : Closeable {
//
//    internal var opened: Boolean = true
//    private set
//
//    final override fun close() {
//        opened = false
//    }
//
//    internal fun checkOpen() { check(opened) { "${this::class.simpleName} is closed" } }
//
//    internal fun open() {
//        opened = true
//    }
//
//}
