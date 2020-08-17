package org.kodein.db.test.utils

import org.kodein.log.LogFrontend
import org.kodein.log.Logger
import kotlin.reflect.KClass

class AssertLogger {
    val entries = ArrayList<Triple<Logger.Tag, Logger.Entry, String?>>()

    val frontEnd: LogFrontend = { c ->
        { e, m ->
            entries += Triple(c, e, m)
        }
    }

}
