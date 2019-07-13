package org.kodein.db.model

import org.kodein.db.Options

interface Cache {

    object Skip : Options.Read, Options.Write

    object Refresh : Options.Read

    data class CopyMaxSize(val size: Int) : Options.Read

}
