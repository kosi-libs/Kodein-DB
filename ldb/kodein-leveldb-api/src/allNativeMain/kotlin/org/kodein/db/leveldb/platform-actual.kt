package org.kodein.db.leveldb


public actual class StackTrace(private val elements: Array<String>) {

    public actual fun write(on: Appendable) {
        on.append(elements.joinToString("\n"))
    }

    public actual companion object {
        public actual fun current(): StackTrace = StackTrace(Exception().getStackTrace())
    }

}

public actual fun <T> newWeakHashSet(): MutableSet<T> = HashSet()
