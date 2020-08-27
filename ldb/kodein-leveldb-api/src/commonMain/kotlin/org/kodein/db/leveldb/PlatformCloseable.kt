package org.kodein.db.leveldb

import kotlinx.atomicfu.atomic
import org.kodein.log.newLogger
import org.kodein.memory.Closeable


public abstract class PlatformCloseable(private val name: String, private val handler: Handler?, public val options: LevelDB.Options) : Closeable {

    private val stackTrace = if (options.trackClosableAllocation) {
        StackTrace.current()
    } else {
        null
    }

    private val closed = atomic(false)

    init {
        @Suppress("LeakingThis")
        handler?.add(this)
    }

    protected abstract fun platformClose()

    protected open fun beforeClose() {}

    private fun doClose() {
        beforeClose()
        handler?.remove(this)
        platformClose()
    }

    final override fun close() {
        if (closed.getAndSet(true))
            return
        doClose()
    }

    public fun checkIsOpen(): Unit = check(!closed.value) { "$name has been closed" }

    public fun closeBad() {
        if (closed.getAndSet(true))
            return

        val logger = options.loggerFactory.newLogger(this::class)
        if (stackTrace == null) {
            if (!options.failOnBadClose) logger.warning { "$name has not been properly closed. To track its allocation, set trackClosableAllocation." }
            doClose()
            if (options.failOnBadClose) error("$name has not been properly closed. To track its allocation, set trackClosableAllocation.")
            return
        }

        val message = StringBuilder("$name must be closed. Creation stack trace:\n")
        stackTrace.write(message)

        if (!options.failOnBadClose) logger.warning { message.toString() }
        else error(message.toString())

        doClose()
    }

    @Suppress("unused")
    protected fun finalize() {
        if (!closed.value)
            closeBad()
    }

    public class Handler : Closeable {

        private val closed = atomic(false)

        private val set = newWeakHashSet<PlatformCloseable>()

        public fun add(pc: PlatformCloseable) {
            if (closed.value)
                return
            set.add(pc)
        }

        public fun remove(pc: PlatformCloseable) {
            if (!closed.value)
                set.remove(pc)
        }

        override fun close() {
            if (closed.getAndSet(true))
                return

            set.forEach { it.closeBad() }
            set.clear()
            closed.value = false
        }

    }

}
