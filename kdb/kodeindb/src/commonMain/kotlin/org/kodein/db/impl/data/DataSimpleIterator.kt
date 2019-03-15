//package org.kodein.db.impl.data
//
//import org.kodein.db.data.DataIterator
//import org.kodein.db.leveldb.*
//
//class DataSimpleIterator internal constructor(it: LevelDB.Cursor, prefix: Bytes) : AbstractDataIterator(it, prefix) {
//
//    override fun nextEntries(size: Int): DataIterator.Entries {
//        cacheReset()
//        return Entries(it.nextArray(size))
//    }
//
//    override fun thisKey() = itKey()
//
//    override fun thisValue() = it.transientValue().toAllocation()
//
//    private inner class Entries internal constructor(array: LevelDB.Cursor.ValuesArray) : AbstractEntries<LevelDB.Cursor.ValuesArray>(array) {
//
//        override fun thisSeekKey(i: Int) = arrayKey(i)
//
//        override fun thisKey(i: Int) = arrayKey(i)
//
//        override fun thisValue(i: Int) = array.getValue(i)
//    }
//}
