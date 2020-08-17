package org.kodein.db

import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.wrap
import kotlin.reflect.KClass

public interface TypeTable : Options.Open {

    public fun getTypeName(type: KClass<*>): ReadMemory

    public fun getTypeClass(name: ReadMemory): KClass<*>?

    public fun getRegisteredClasses(): Set<KClass<*>>

    public fun getRootOf(type: KClass<*>): KClass<*>?

    private class Impl(roots: Iterable<Type.Root<*>>, val defaultNameOf: (KClass<*>) -> ReadMemory, val defaultClassOf: (ReadMemory) -> KClass<*>?) : TypeTable {

        private val byClass = HashMap<KClass<*>, Type<*>>()

        private val byName = HashMap<ReadMemory, Type<*>>()

        private val roots = HashMap<KClass<*>, Type.Root<*>>()

        init {

            fun add(type: Type<*>, root: Type.Root<*>) {
                require(type.name !in byName) { "Both type ${simpleTypeNameOf(byName[type.name]!!.type)} and type ${simpleTypeNameOf(type.type)} are mapped to name ${type.name}" }
                require(type.type !in byClass) { "Type ${simpleTypeNameOf(type.type)} is mapped to both name ${byClass[type.type]!!} and name ${type.name}" }

                this.byClass[type.type] = type
                this.byName[type.name] = type
                this.roots[type.type] = root
            }

            roots.forEach { root ->
                root.subs.forEach { add(it, root) }
                add(root, root)
            }

        }

        override fun getTypeName(type: KClass<*>) = byClass[type]?.name ?: defaultNameOf(type)

        override fun getTypeClass(name: ReadMemory): KClass<*>? = byName[name]?.type ?: defaultClassOf(name)

        override fun getRegisteredClasses(): Set<KClass<*>> = byClass.keys

        override fun getRootOf(type: KClass<*>): KClass<*>? = roots[type]?.type

    }

    public sealed class Type<T : Any>(public val type: KClass<T>, public val name: ReadMemory) {
        public class Sub<T : Any>(type: KClass<T>, name: ReadMemory) : Type<T>(type, name)
        public class Root<T : Any>(type: KClass<T>, name: ReadMemory, public val subs: List<Sub<out T>>) : Type<T>(type, name)
    }

    public class Builder internal constructor(public val defaultNameOf: (KClass<*>) -> ReadMemory) {

        internal val roots = ArrayList<Type.Root<*>>()

        public fun <T: Any> root(type: KClass<T>, name: ReadMemory = defaultNameOf(type)): Root<T> {
            val root = Root<T>(defaultNameOf)
            roots += Type.Root(type, name, root.subs)
            return root
        }

        public inline fun <reified T: Any> root(name: ReadMemory = defaultNameOf(T::class)): Root<T> = root(T::class, name)

        public class Root<T : Any>(public val defaultNameOf: (KClass<*>) -> ReadMemory) {

            internal val subs = ArrayList<Type.Sub<out T>>()

            public fun <S : T> sub(type: KClass<S>, name: ReadMemory = defaultNameOf(type)): Root<T> = apply { subs += Type.Sub(type, name) }

            public inline fun <reified S: T> sub(name: ReadMemory = defaultNameOf(S::class)): Root<T> = sub(S::class, name)
        }
    }

    public companion object {
        public operator fun invoke(defaultNameOf: (KClass<*>) -> ReadMemory = { KBuffer.wrap(simpleTypeAsciiNameOf(it)) }, defaultClassOf: (ReadMemory) -> KClass<*>? = { null }, builder: Builder.() -> Unit = {}): TypeTable = Impl(Builder(defaultNameOf).apply(builder).roots, defaultNameOf, defaultClassOf)
    }
}
