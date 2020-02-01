package org.kodein.db.impl.model

import org.kodein.db.BaseCursor
import org.kodein.memory.io.ReadMemory

interface ResettableCursorModule : BaseCursor {
    val cursor: BaseCursor

    fun reset()

    override fun isValid() = cursor.isValid()

    override fun next() {
        reset()
        cursor.next()
    }

    override fun prev() {
        reset()
        cursor.prev()
    }

    override fun seekToFirst() {
        reset()
        cursor.seekToFirst()
    }

    override fun seekToLast() {
        reset()
        cursor.seekToLast()
    }

    override fun seekTo(target: ReadMemory) {
        reset()
        cursor.seekTo(target)
    }

    override fun transientSeekKey() = cursor.transientSeekKey()
}
