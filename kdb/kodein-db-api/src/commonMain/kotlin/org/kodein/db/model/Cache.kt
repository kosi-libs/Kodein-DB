package org.kodein.db.model

import org.kodein.db.Options

interface Cache {

    object Skip : Options.Write

    data class CopyMaxSize(val size: Int) : Options.Read

}
