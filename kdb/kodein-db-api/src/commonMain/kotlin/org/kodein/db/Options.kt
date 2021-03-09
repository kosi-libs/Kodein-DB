package org.kodein.db
import org.kodein.db.data.DataRead
import org.kodein.db.data.DataSnapshot
import org.kodein.db.data.DataWrite
import org.kodein.db.leveldb.LevelDB
import kotlin.collections.plus as kotlinPlus

public interface Options {

    public interface Read : Options

    public interface Write : Options

    public interface Open : Options
}

public inline operator fun <reified T : Options> Array<out Options>.invoke(): T? = firstOrNull { it is T } as T?
public inline fun <reified T : Options> Array<out Options>.all(): List<T> = filterIsInstance<T>()

@Suppress("UNCHECKED_CAST")
public operator fun <T : Options> Array<out T>.plus(add: T): Array<T> = (this as Array<T>).kotlinPlus(add)

public fun LevelDB.WriteOptions.Companion.from(options: Array<out Options.Write>): LevelDB.WriteOptions {
    val syncOption: DataWrite.Sync = options() ?: return DEFAULT
    return LevelDB.WriteOptions(sync = syncOption.sync)
}

public fun LevelDB.ReadOptions.Companion.from(snapshot: LevelDB.Snapshot?, options: Array<out Options.Read>): LevelDB.ReadOptions =
    options.fold(
        snapshot?.let { LevelDB.ReadOptions(snapshot = it) } ?: DEFAULT
    ) { l, o ->
        when (o) {
            is DataRead.VerifyChecksum -> l.copy(verifyChecksums = o.verifyChecksums)
            is DataRead.FillCache -> l.copy(fillCache = o.fillCache)
            else -> l
        }
    }
