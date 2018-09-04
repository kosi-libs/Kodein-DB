package org.kodein.db.leveldb

import kotlinx.io.core.Closeable


abstract class PlatformCloseable(private val name: String, val handler: Handler?, val options: LevelDB.Options) : Closeable {

    private val stackTrace: StackTrace?

    abstract val isClosed: Boolean

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

    override fun close() {
        if (isClosed)
            return
        synchronized(this) {
            if (isClosed)
                return
            beforeClose()
            handler?.remove(this)
            platformClose()
        }
    }

    fun checkIsOpen() {
        if (isClosed)
            throw IllegalStateException("$name has been closed")
    }

    @Synchronized
    fun closeBad() {
        if (isClosed)
            return

        val logger = options.loggerFactory?.invoke(this::class)
        if (logger != null) {
            if (stackTrace == null) {
                logger.warning("$name has not been properly closed. To track its allocation, open the DB with trackClosableAllocation = true")
                close()
                return
            }

            val message = StringBuilder("$name must be closed. Creation stack trace:")
            stackTrace.write(message)
            logger.warning(message.toString())
        }

        close()
    }

    @Suppress("unused")
    protected fun finalize() {
        if (!isClosed)
            closeBad()
    }

    class Handler : Closeable {

        private var closing = false

        private val set = WeakHashSet<PlatformCloseable>()

        fun add(pc: PlatformCloseable) {
            set.add(pc)
        }

        fun remove(pc: PlatformCloseable) {
            if (!closing) set.remove(pc)
        }

        @Synchronized
        override fun close() {
            closing = true

            set.forEach { it.closeBad() }
            set.clear()

            closing = false
        }

    }

}
