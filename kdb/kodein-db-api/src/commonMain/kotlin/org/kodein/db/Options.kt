package org.kodein.db

public interface Options {

    public interface Open : Options

    public interface Get : Options
    public interface Find : Options

    public interface NewSnapshot : Options

    public interface DirectPut : Options
    public interface DirectDelete : Options

    public interface NewBatch : Options

    public interface BatchPut : Options
    public interface BatchDelete : Options
    public interface BatchWrite : Options

    public interface Puts : DirectPut, BatchPut
    public interface Deletes : DirectDelete, BatchDelete
    public interface Writes : DirectPut, DirectDelete, BatchWrite
    public interface Reads : Get, Find

    // Deprecated since 0.8.0
    @Deprecated("Use either or a combination of new Options", level = DeprecationLevel.ERROR)
    public interface Read : Reads, NewSnapshot

    // Deprecated since 0.8.0
    @Deprecated("Use either or a combination of new Options", level = DeprecationLevel.ERROR)
    public interface Write : Puts, Deletes, Writes

    public interface User : Writes
}

public inline operator fun <reified T : Options> Array<out Options>.invoke(): T? = firstOrNull { it is T } as T?
public inline fun <reified T : Options> Array<out Options>.all(): List<T> = filterIsInstance<T>()

@Suppress("UNCHECKED_CAST")
public inline operator fun <reified T : Options> Array<out T>.plus(add: T): Array<T> =
    Array(size + 1) {
        when (it) {
            in indices -> this[it]
            size -> add
            else -> error("This should never happen")
        }
    }

public interface UserOptions : Options {
    public interface Put : UserOptions, Options.Puts
}