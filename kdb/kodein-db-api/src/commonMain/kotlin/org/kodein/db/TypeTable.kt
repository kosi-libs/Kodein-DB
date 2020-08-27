package org.kodein.db

import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.wrap
import kotlin.reflect.KType
import kotlin.reflect.typeOf

public interface TypeTable : Options.Open {

    public fun getTypeName(type: KType): ReadMemory

    public fun getType(name: ReadMemory): KType?

    public fun getRegisteredTypes(): Set<KType>

    public fun getRootOf(type: KType): KType?

    private class Impl(roots: Iterable<Type.Root<*>>, val defaultNameOf: (KType) -> ReadMemory, val defaultTypeOf: (ReadMemory) -> KType?) : TypeTable {

        private val byClass = HashMap<KType, Type<*>>()

        private val byName = HashMap<ReadMemory, Type<*>>()

        private val roots = HashMap<KType, Type.Root<*>>()

        init {

            fun add(type: Type<*>, root: Type.Root<*>) {
                require(type.name !in byName) { "Both type ${simpleTypeNameOf(byName[type.name]!!.ktype)} and type ${simpleTypeNameOf(type.ktype)} are mapped to name ${type.name}" }
                require(type.ktype !in byClass) { "Type ${simpleTypeNameOf(type.ktype)} is mapped to both name ${byClass[type.ktype]!!} and name ${type.name}" }

                this.byClass[type.ktype] = type
                this.byName[type.name] = type
                this.roots[type.ktype] = root
            }

            roots.forEach { root ->
                root.subs.forEach { add(it, root) }
                add(root, root)
            }

        }

        override fun getTypeName(type: KType) = byClass[type]?.name ?: defaultNameOf(type)

        override fun getType(name: ReadMemory): KType? = byName[name]?.ktype ?: defaultTypeOf(name)

        override fun getRegisteredTypes(): Set<KType> = byClass.keys

        override fun getRootOf(type: KType): KType? = roots[type]?.ktype

    }

    public sealed class Type<T : Any>(private val tktype: TKType<T>, public val name: ReadMemory) {
        public class Sub<T : Any>(type: TKType<T>, name: ReadMemory) : Type<T>(type, name)
        public class Root<T : Any>(type: TKType<T>, name: ReadMemory, public val subs: List<Sub<out T>>) : Type<T>(type, name)
        public val ktype: KType get() = tktype.ktype
    }

    public class Builder internal constructor(public val defaultNameOf: (KType) -> ReadMemory) {

        internal val roots = ArrayList<Type.Root<*>>()

        public fun <T: Any> root(type: TKType<T>, name: ReadMemory = defaultNameOf(type.ktype)): Root<T> {
            val root = Root<T>(defaultNameOf)
            roots += Type.Root(type, name, root.subs)
            return root
        }

        @OptIn(ExperimentalStdlibApi::class)
        public inline fun <reified T: Any> root(name: ReadMemory = defaultNameOf(typeOf<T>())): Root<T> = root(tTypeOf(), name)

        public class Root<T : Any>(public val defaultNameOf: (KType) -> ReadMemory) {

            internal val subs = ArrayList<Type.Sub<out T>>()

            public fun <S : T> sub(type: TKType<S>, name: ReadMemory = defaultNameOf(type.ktype)): Root<T> = apply { subs += Type.Sub(type, name) }

            @OptIn(ExperimentalStdlibApi::class)
            public inline fun <reified S: T> sub(name: ReadMemory = defaultNameOf(typeOf<S>())): Root<T> = sub(tTypeOf<S>(), name)
        }
    }

    public companion object {
        public operator fun invoke(defaultNameOf: (KType) -> ReadMemory = { KBuffer.wrap(simpleTypeAsciiNameOf(it)) }, defaultClassOf: (ReadMemory) -> KType? = { null }, builder: Builder.() -> Unit = {}): TypeTable = Impl(Builder(defaultNameOf).apply(builder).roots, defaultNameOf, defaultClassOf)
    }
}
