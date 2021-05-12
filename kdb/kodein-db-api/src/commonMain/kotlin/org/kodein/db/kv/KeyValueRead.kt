package org.kodein.db.kv

import org.kodein.db.Options
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.ReadMemory


public interface KeyValueRead {

    public fun get(key: ReadMemory, vararg options: Options.Get): Allocation?

    public fun newCursor(vararg options: Options.Find): KeyValueCursor

}
