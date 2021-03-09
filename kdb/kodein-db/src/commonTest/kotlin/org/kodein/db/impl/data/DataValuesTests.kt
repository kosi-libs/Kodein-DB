package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.array
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalUnsignedTypes
@Suppress("ClassName")
class DataValuesTests {

    @Test
    fun test00_primitives() {
        val value = Value.of(
                Value.of(true),
                Value.of(42.toByte()),
                Value.of(2142.toShort()),
                Value.of(123456789),
                Value.of(1234567890123456789L)
        )
        assertEquals(20, value.size)

        val buffer = KBuffer.array(value.size) { value.writeInto(this) }

        assertBytesEquals(
                ubyteArrayOf(
                        0x01u,
                        0x00u,
                        0x02Au,
                        0x00u,
                        0x08u, 0x5Eu,
                        0x00u,
                        0x07u, 0x5Bu, 0xCDu, 0x15u,
                        0x00u,
                        0x11u, 0x22u, 0x10u, 0xF4u, 0x7Du, 0xE9u, 0x81u, 0x15u
                ).toByteArray(),
                buffer,
                false
        )
    }

    @Test
    fun test01_bytes() {
        val value = Value.of(
                Value.of(byteArrayOf(1, 2, 3, 4)),
                Value.of(KBuffer.array(4) { putBytes(byteArrayOf(5, 6, 7, 8)) })
        )
        assertEquals(9, value.size)

        val buffer = KBuffer.array(value.size) { value.writeInto(this) }

        assertBytesEquals(
                ubyteArrayOf(
                        0x01u, 0x02u, 0x03u, 0x04u,
                        0x00u,
                        0x05u, 0x06u, 0x07u, 0x08u
                ).toByteArray(),
                buffer,
                false
        )
    }

    @Test
    fun test02_text() {
        val value = Value.of(
                Value.of("B", "R", "Y", "S"),
                Value.of("Salomon")
        )
        assertEquals(15, value.size)

        val buffer = KBuffer.array(value.size) { value.writeInto(this) }

        assertBytesEquals(
                ubyteArrayOf(
                        0x42u, 0x00u, 0x52u, 0x00u, 0x59u, 0x00u, 0x53u,
                        0x00u,
                        0x53u, 0x61u, 0x6Cu, 0x6Fu, 0x6Du, 0x6Fu, 0x6Eu
                ).toByteArray(),
                buffer,
                false
        )
    }

    @Test
    fun test02_all() {
        val value = Value.of(
            Value.of(true),
            Value.of(42.toByte()),
            Value.of(2142.toShort()),
            Value.of(123456789),
            Value.of(1234567890123456789L),
            Value.emptyValue,
            Value.of(byteArrayOf(1, 2, 3, 4)),
            Value.of(KBuffer.array(4) { putBytes(byteArrayOf(5, 6, 7, 8)) }),
            Value.of('B', 'R', 'Y', 'S'),
            Value.of("Salomon")
        )
        assertEquals(47, value.size)

        val buffer = KBuffer.array(value.size) { value.writeInto(this) }

        assertBytesEquals(
                ubyteArrayOf(
                        0x01u,
                        0x00u,
                        0x02Au,
                        0x00u,
                        0x08u, 0x5Eu,
                        0x00u,
                        0x07u, 0x5Bu, 0xCDu, 0x15u,
                        0x00u,
                        0x11u, 0x22u, 0x10u, 0xF4u, 0x7Du, 0xE9u, 0x81u, 0x15u,
                        0x00u,
                        0x00u,
                        0x01u, 0x02u, 0x03u, 0x04u,
                        0x00u,
                        0x05u, 0x06u, 0x07u, 0x08u,
                        0x00u,
                        0x42u, 0x00u, 0x52u, 0x00u, 0x59u, 0x00u, 0x53u,
                        0x00u,
                        0x53u, 0x61u, 0x6Cu, 0x6Fu, 0x6Du, 0x6Fu, 0x6Eu
                ).toByteArray(),
                buffer,
                false
        )
    }
}
