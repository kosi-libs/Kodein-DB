package org.kodein.db

import org.kodein.db.kv.KeyValueBatch
import org.kodein.db.leveldb.LevelDB

public class Anticipate(public val block: () -> Unit) : Options.Writes

public class AnticipateInLock(public val block: (KeyValueBatch) -> Unit) : Options.Writes

public class React(public val block: (Int) -> Unit) : Options.Writes

public class ReactInLock(public val block: (Int) -> Unit) : Options.Writes
