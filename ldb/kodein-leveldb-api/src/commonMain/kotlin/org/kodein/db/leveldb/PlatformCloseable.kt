package org.kodein.db.leveldb

import kotlinx.atomicfu.atomic
import kotlinx.io.core.Closeable


abstract class PlatformCloseable(private val name: String, val handler: Handler?, val options: LevelDB.Options) : Closeable {

    private val stackTrace: StackTrace?

    private val closed = atomic(false)

    init {
        if (options.loggerFactory != null && options.trackClosableAllocation) {
            stackTrace = StackTrace.current()
        } else {
            stackTrace = null
        }

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

    fun checkIsOpen() {
        if (closed.value)
            throw IllegalStateException("$name has been closed")
    }

    fun closeBad() {
        if (closed.getAndSet(true))
            return

        val logger = options.loggerFactory?.invoke(this::class)
        if (logger != null) {
            if (stackTrace == null) {
                logger.warning("$name has not been properly closed. To track its allocation, open the DB with trackClosableAllocation = true")
                doClose()
                return
            }

            val message = StringBuilder("$name must be closed. Creation stack trace:\n")
            stackTrace.write(message)
            logger.warning(message.toString())
        }

        doClose()
    }

    @Suppress("unused")
    protected fun finalize() {
        if (!closed.value)
            closeBad()
    }

    class Handler : Closeable {

        private val closed = atomic(false)

        private val set = WeakHashSet<PlatformCloseable>()

        fun add(pc: PlatformCloseable) {
            if (closed.value)
                return
            set.add(pc)
        }

        fun remove(pc: PlatformCloseable) {
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
