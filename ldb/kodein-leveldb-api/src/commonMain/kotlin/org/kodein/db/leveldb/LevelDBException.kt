package org.kodein.db.leveldb

/**
 * Exception thrown if something went wrong in a LevelDB operation.
 */
public class LevelDBException(message: String) : Exception(message) {
    //    constructor(e: Throwable) : super(e)
}
