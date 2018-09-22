package org.kodein.db.data

import org.kodein.db.WriteType
import org.kodein.db.leveldb.Bytes

class DataConcurrentModificationException(type: String, expectedVersion: Int, actualVersion: Int, operationType: WriteType, val key: Bytes)
    : Exception(type + ' ' + operationType.name + " failed: Expected version " + expectedVersion + " but was version " + actualVersion)
