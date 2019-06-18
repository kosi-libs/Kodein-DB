package org.kodein.db.impl.model.cache

import org.kodein.db.model.Key
import org.kodein.memory.model.ObjectCache

typealias ModelCache = ObjectCache<Key<*>, Any>
