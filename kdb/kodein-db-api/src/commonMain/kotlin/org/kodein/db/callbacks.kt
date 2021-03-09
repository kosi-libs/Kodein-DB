package org.kodein.db

import org.kodein.db.leveldb.LevelDB

public class Anticipate(public val block: () -> Unit) : Options.Write

public class AnticipateInLock(public val block: (LevelDB.WriteBatch) -> Unit) : Options.Write

public class React(public val block: (Int) -> Unit) : Options.Write

public class ReactInLock(public val block: (Int) -> Unit) : Options.Write
