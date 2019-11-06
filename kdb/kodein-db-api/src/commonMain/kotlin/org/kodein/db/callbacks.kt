package org.kodein.db

class Check(val needsLock: Boolean = false, val block: () -> Unit) : Options.Write

class React(val needsLock: Boolean = false, val block: (Int) -> Unit) : Options.Write
