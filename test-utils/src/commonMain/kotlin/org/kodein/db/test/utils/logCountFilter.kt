package org.kodein.db.test.utils

import org.kodein.log.Logger
import org.kodein.log.LoggerFilter

class AssertLogger {
    var count = 0
    var last: Logger.Entry? = null

    val filter: LoggerFilter = {
        ++count
        last = it
        it
    }

}