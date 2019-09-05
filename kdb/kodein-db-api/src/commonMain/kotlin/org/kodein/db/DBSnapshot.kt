package org.kodein.db

import org.kodein.memory.Closeable

interface DBSnapshot : DBRead, Closeable
