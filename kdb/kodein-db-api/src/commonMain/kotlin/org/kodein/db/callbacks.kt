package org.kodein.db

public class Anticipate(public val needsLock: Boolean = false, public val block: () -> Unit) : Options.Write

public class React(public val needsLock: Boolean = false, public val block: (Int) -> Unit) : Options.Write
