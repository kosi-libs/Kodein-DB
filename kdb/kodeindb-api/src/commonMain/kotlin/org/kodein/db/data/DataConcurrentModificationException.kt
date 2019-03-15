package org.kodein.db.data

import org.kodein.db.WriteType
import org.kodein.db.leveldb.Bytes

class DataConcurrentModificationException(val type: String, val expectedVersion: Int, val actualVersion: Int, val operationType: WriteType, val key: Bytes)
    : Exception(type + ' ' + operationType.name + " failed: Expected version " + expectedVersion + " but was version " + actualVersion)
