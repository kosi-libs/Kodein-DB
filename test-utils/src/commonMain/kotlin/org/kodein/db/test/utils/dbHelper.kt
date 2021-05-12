package org.kodein.db.test.utils

import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.io.getBytes
import org.kodein.memory.use
import kotlin.test.fail


fun assertDBIs(ldb: LevelDB, vararg keyValues: Pair<ByteArray, ByteArray>) {
    ldb.newCursor().use { cursor ->
        cursor.seekToFirst()
        var i = 0
        while (cursor.isValid()) {
            if (i >= keyValues.size) {
                fail("DB contains additional entrie(s): " + cursor.transientKey().getBytes().description())
            }
            assertBytesEquals(keyValues[i].first, cursor.transientKey(), prefix = "Key ${i + 1}: ")
            assertBytesEquals(keyValues[i].second, cursor.transientValue(), prefix = "Value ${i + 1}: ")
            cursor.next()
            i++
        }
        if (i < keyValues.size) {
            fail("DB is missing entrie(s):\n" + keyValues.takeLast(keyValues.size - i).joinToString("\n") { it.first.description() + ": " + it.second.description() })
        }
    }
}
