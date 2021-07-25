package org.kodein.db.index

import org.kodein.db.DB
import org.kodein.db.deleteAll

/**
 * Deletes all elements in this database matching the given [filter].
 */
public inline fun <reified M : Any> DB.delete(filter: Filter<M>) {
    deleteAll(filter.find(this))
}
