package org.kodein.db.encryption

import org.kodein.db.Value
import org.kodein.db.test.utils.array
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.hex
import org.kodein.db.test.utils.int
import kotlin.test.Test


class EncryptionTests_00_Keys : EncryptionTests() {

    @Test
    fun test00_hashedKey() {
        assertBytesEquals(array('o', 0, int(1), hex("c9f2af60f2497f86d51986c9b67410f9e7f7947561e09741052e6ca42863fd76"), 0), ddb.newKey(1, Value.of("Id1")))
        assertBytesEquals(array('o', 0, int(4), hex("4d4d4caea1a37153420c1f05b905ba6d608883c788c52b4cf1074000f48721a6"), 0), ddb.newKey(4, Value.of("Id4")))
    }

    @Test
    fun test01_NonHashedKey() {
        assertBytesEquals(array('o', 0, int(2), "Id2", 0), ddb.newKey(2, Value.of("Id2")))
        assertBytesEquals(array('o', 0, int(3), "Id3", 0), ddb.newKey(3, Value.of("Id3")))
    }

}
