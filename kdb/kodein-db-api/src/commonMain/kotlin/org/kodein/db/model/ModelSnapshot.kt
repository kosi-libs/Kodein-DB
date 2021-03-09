package org.kodein.db.model

import org.kodein.db.data.DataSnapshot
import org.kodein.memory.Closeable

public interface ModelSnapshot : ModelRead, Closeable {

    public val data: DataSnapshot

}
