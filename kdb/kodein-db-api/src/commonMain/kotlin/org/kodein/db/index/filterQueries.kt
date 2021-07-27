package org.kodein.db.index

import org.kodein.db.*
import org.kodein.memory.use

/**
 * Returns a [Cursor] where all elements match the
 * given [filter].
 */
public fun <M : Any> DBRead.find(filter: Filter<M>): Cursor<M> = filter.find(this)

/**
 * Checks if at least one element in the database matches
 * the given filter.
 *
 * @return true, if one element matches the [filter]
 */
public fun DBRead.any(filter: Filter<*>): Boolean = find(filter).any()

/**
 * Checks if no element in the database matches the given
 * filter.
 *
 * @return true, if no element matches the [filter] or if there
 * are no elements in the database
 */
public fun DBRead.none(filter: Filter<*>): Boolean = find(filter).none()

/**
 * Returns a [List] containing all elements in the database
 * matching the given [filter].
 */
public fun <M : Any> DBRead.findModelList(filter: Filter<M>): List<M> = find(filter).toModelList()

/**
 * Returns a [List] containing all element keys in the database
 * matching the given [filter].
 */
public fun <M : Any> DBRead.findKeyList(filter: Filter<M>): List<Key<M>> = find(filter).toKeyList()

/**
 * Returns a [List] containing all element entries in the database
 * matching the given [filter].
 */
public fun <M : Any> DBRead.findEntryList(filter: Filter<M>): List<Entry<M>> = find(filter).toEntryList()

/**
 * Returns the first element in the database matching the
 * given [filter].
 *
 * @throws NoSuchElementException if the cursor is empty.
 */
public fun <M : Any> DBRead.findOne(filter: Filter<M>): M = find(filter).first()

/**
 * Returns the first element in the database matching the
 * given [filter] or null of there is no such element.
 */
public fun <M : Any> DBRead.findOneOrNull(filter: Filter<M>): M? = filter.find(this).firstOrNull()
