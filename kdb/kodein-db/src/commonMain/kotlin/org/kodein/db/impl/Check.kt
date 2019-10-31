package org.kodein.db.impl

import org.kodein.db.Options

class Check(val block: () -> Unit) : Options.Write
