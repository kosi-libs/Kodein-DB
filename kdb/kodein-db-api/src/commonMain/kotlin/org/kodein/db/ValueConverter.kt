package org.kodein.db

import org.kodein.memory.util.UUID

public fun interface ValueConverter : Options.Open {

    public fun toValue(from: Any): Value?

    public companion object {
        public inline fun <reified T : Any> forClass(crossinline converter: (from: T) -> Value): ValueConverter =
            ValueConverter { from ->
                if (from is T) converter(from)
                else null
            }

        public val uuid: ValueConverter = forClass<UUID> { Value.of(it.mostSignificantBits, it.leastSignificantBits) }

        public val defaults: List<ValueConverter> = listOf(uuid)
    }
}
