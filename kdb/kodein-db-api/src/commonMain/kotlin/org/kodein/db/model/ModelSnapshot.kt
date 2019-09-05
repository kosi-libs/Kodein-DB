package org.kodein.db.model

import org.kodein.memory.Closeable

interface ModelSnapshot : ModelRead, Closeable
