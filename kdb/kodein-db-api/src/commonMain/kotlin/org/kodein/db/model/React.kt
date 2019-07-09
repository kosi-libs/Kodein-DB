package org.kodein.db.model

import org.kodein.db.Options

interface React {

    object Init : Options.React

    interface Subscription {
        fun stop()
    }

    enum class Operation {
        Put, Delete
    }

}
