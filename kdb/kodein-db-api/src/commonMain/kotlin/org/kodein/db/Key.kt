@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package org.kodein.db

import org.kodein.memory.io.ReadBuffer


class Key<out T : Any>(val bytes: ReadBuffer) {
    override fun hashCode(): Int = bytes.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Key<*>) return false
        return bytes == other.bytes
    }
}
