package org.kodein.db

import org.kodein.memory.Closeable

interface Snapshot : DBRead, Closeable
