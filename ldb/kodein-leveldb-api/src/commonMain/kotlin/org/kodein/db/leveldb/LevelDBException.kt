package org.kodein.db.leveldb

/**
 * Exception thrown if something went wrong in a LevelDB operation.
 */
class LevelDBException : Exception {
    constructor(message: String) : super(message)
    constructor(e: Throwable) : super(e)
}
