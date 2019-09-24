package org.kodein.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.kodein.memory.use


interface AsyncCursor<M: Any> : BaseCursor {

    suspend fun transientKey(): TransientKey<M>
    suspend fun model(vararg options: Options.Read): M

    suspend fun nextEntries(size: Int): Entries<M>

    interface Entries<M: Any> : BaseCursor.BaseEntries {
        fun key(i: Int): Key<M>
        suspend fun get(i: Int, vararg options: Options.Read): M
    }

    fun sync(): Cursor<M>
}


fun <M : Any> AsyncCursor<M>.models(): Flow<M> = flow {
    use {
        while (isValid())
            emit(model())
    }
}

fun <M : Any> AsyncCursor.Entries<M>.models(): Flow<M> = flow {
    for (i in 0 until size)
        emit(get(i))
}
