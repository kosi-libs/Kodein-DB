package org.kodein.db.encryption

import org.kodein.db.Value
import org.kodein.db.test.utils.array
import org.kodein.db.test.utils.hex
import org.kodein.db.test.utils.int
import org.kodein.db.test.utils.ushort
import org.kodein.memory.io.Memory
import org.kodein.memory.text.array
import kotlin.test.Test

class EncryptionTests_01_Puts : EncryptionTests() {

    @Test
    fun test00_NotEncrypted() {
        ddb.put(
            ddb.newKey(2, Value.of("Id2")),
            Value.of("Body2"),
            mapOf(
                "Symbols" to listOf(
                    Value.of("alpha") to Value.of("Meta2A"),
                    Value.of("beta") to Value.of("Meta2B")
                )
            )
        )

        assertDBIs(
            array('i', 0, int(2), "Symbols", 0, "alpha", 0, "Id2", 0) to array(128, ushort(5), ushort(3), "Meta2A"),
            array('i', 0, int(2), "Symbols", 0, "beta", 0, "Id2", 0) to array(128, ushort(4), ushort(3), "Meta2B"),
            array('o', 0, int(2), "Id2", 0) to array("Body2"),
            array('r', 0, int(2), "Id2", 0) to array(128, "Symbols", 0, int(13), ushort(5), "alpha", ushort(4), "beta")
        )
    }

    @Test
    fun test01_OnlyBody() {
        ddb.put(
            ddb.newKey(3, Value.of("Id3")),
            Value.of("Body3"),
            mapOf(
                "Symbols" to listOf(
                    Value.of("alpha") to Value.of("Meta3A"),
                    Value.of("beta") to null
                )
            ),
            IV(Memory.array("A cool Test IV!!"))
        )

        assertDBIs(
            array('i', 0, int(3), "Symbols", 0, "alpha", 0, "Id3", 0) to array(128, ushort(5), ushort(3), "A cool Test IV!!", hex("4743f8d279c20e272dc6a00255fd9dae")),
            array('i', 0, int(3), "Symbols", 0, "beta", 0, "Id3", 0) to array(128, ushort(4), ushort(3)),
            array('o', 0, int(3), "Id3", 0) to array("A cool Test IV!!", hex("da3a8c40475df7da58890e7d49201ec0")),
            array('r', 0, int(3), "Id3", 0) to array(128, "Symbols", 0, int(13), ushort(5), "alpha", ushort(4), "beta")
        )
    }

    @Test
    fun test02_BodyAndId() {
        ddb.put(
            ddb.newKey(4, Value.of("Id4")),
            Value.of("Body4"),
            mapOf(
                "Symbols" to listOf(
                    Value.of("alpha") to Value.of("Meta4A"),
                    Value.of("beta") to null
                )
            ),
            IV(Memory.array("A cool Test IV!!"))
        )

        assertDBIs(
            array('i', 0, int(4), "Symbols", 0, "alpha", 0, hex("4d4d4caea1a37153420c1f05b905ba6d608883c788c52b4cf1074000f48721a6"), 0) to array(128, ushort(5), ushort(32), "A cool Test IV!!", hex("4aee5c95e8f3474207ec02a1e52ffcfd")),
            array('i', 0, int(4), "Symbols", 0, "beta", 0, hex("4d4d4caea1a37153420c1f05b905ba6d608883c788c52b4cf1074000f48721a6"), 0) to array(128, ushort(4), ushort(32)),
            array('o', 0, int(4), hex("4d4d4caea1a37153420c1f05b905ba6d608883c788c52b4cf1074000f48721a6"), 0) to array("A cool Test IV!!", hex("dff952b3a9a2b60dfd4ba9a9941ea9a6")),
            array('r', 0, int(4), hex("4d4d4caea1a37153420c1f05b905ba6d608883c788c52b4cf1074000f48721a6"), 0) to array(128, "Symbols", 0, int(13), ushort(5), "alpha", ushort(4), "beta")
        )
    }

    @Test
    fun test03_AllButAnIndex() {
        ddb.put(
            ddb.newKey(4, Value.of("Id4")),
            Value.of("Body4"),
            mapOf(
                "Symbols" to listOf(
                    Value.of("alpha") to Value.of("Meta4A"),
                    Value.of("beta") to null
                ),
                "Numbers" to listOf(
                    Value.of("fourty", "two") to Value.of("Meta4N")
                )
            ),
            IV(Memory.array("A cool Test IV!!"))
        )

        assertDBIs(
            array('i', 0, int(4), "Numbers", 0, hex("7f93ecbb974acc5ba75ab033527bb2e16528bbf303cffcdd4ba517d1c9669261"), 0, hex("4d4d4caea1a37153420c1f05b905ba6d608883c788c52b4cf1074000f48721a6"), 0) to array(128, ushort(32), ushort(32), "A cool Test IV!!", hex("929915489fa77b9cb7d880d7a34b7ff1")),
            array('i', 0, int(4), "Symbols", 0, "alpha", 0, hex("4d4d4caea1a37153420c1f05b905ba6d608883c788c52b4cf1074000f48721a6"), 0) to array(128, ushort(5), ushort(32), "A cool Test IV!!", hex("4aee5c95e8f3474207ec02a1e52ffcfd")),
            array('i', 0, int(4), "Symbols", 0, "beta", 0, hex("4d4d4caea1a37153420c1f05b905ba6d608883c788c52b4cf1074000f48721a6"), 0) to array(128, ushort(4), ushort(32)),
            array('o', 0, int(4), hex("4d4d4caea1a37153420c1f05b905ba6d608883c788c52b4cf1074000f48721a6"), 0) to array("A cool Test IV!!", hex("dff952b3a9a2b60dfd4ba9a9941ea9a6")),
            array('r', 0, int(4), hex("4d4d4caea1a37153420c1f05b905ba6d608883c788c52b4cf1074000f48721a6"), 0) to array(128, "Symbols", 0, int(13), ushort(5), "alpha", ushort(4), "beta", "Numbers", 0, int(34), ushort(32), hex("7f93ecbb974acc5ba75ab033527bb2e16528bbf303cffcdd4ba517d1c9669261"))
        )
    }

    @Test
    fun test04_All() {
        ddb.put(
            ddb.newKey(1, Value.of("Id1")),
            Value.of("Body1"),
            mapOf(
                "Symbols" to listOf(
                    Value.of("alpha") to Value.of("Meta1A"),
                    Value.of("beta") to null
                )
            ),
            IV(Memory.array("A cool Test IV!!"))
        )

        assertDBIs(
            array('i', 0, int(1), "Symbols", 0, hex("e43f078167cb64192977ff78e8efe90385aad979ba7bdb718713a38b77d5fb2d"), 0, hex("c9f2af60f2497f86d51986c9b67410f9e7f7947561e09741052e6ca42863fd76"), 0) to array(128, ushort(32), ushort(32)),
            array('i', 0, int(1), "Symbols", 0, hex("e827953b1ef0f03b65a6b475f6ec0a84ecd257a236c0e24e8dc6e54287443aa7"), 0, hex("c9f2af60f2497f86d51986c9b67410f9e7f7947561e09741052e6ca42863fd76"), 0) to array(128, ushort(32), ushort(32), "A cool Test IV!!", hex("a50900f4cac8bd1184bf2e5825ae6bac")),
            array('o', 0, int(1), hex("c9f2af60f2497f86d51986c9b67410f9e7f7947561e09741052e6ca42863fd76"), 0) to array("A cool Test IV!!", hex("7c9a36521d222d6a31785113637a3fb2")),
            array('r', 0, int(1), hex("c9f2af60f2497f86d51986c9b67410f9e7f7947561e09741052e6ca42863fd76"), 0) to array(128, "Symbols", 0, int(68), ushort(32), hex("e827953b1ef0f03b65a6b475f6ec0a84ecd257a236c0e24e8dc6e54287443aa7"), ushort(32), hex("e43f078167cb64192977ff78e8efe90385aad979ba7bdb718713a38b77d5fb2d"))
        )
    }
}
