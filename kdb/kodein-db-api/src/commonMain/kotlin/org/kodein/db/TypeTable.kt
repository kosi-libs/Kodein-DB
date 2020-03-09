package org.kodein.db

import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.wrap
import kotlin.reflect.KClass

interface TypeTable : Options.Open {

    fun getTypeName(type: KClass<*>): ReadMemory

    fun getTypeClass(name: ReadMemory): KClass<*>?

    fun getRegisteredClasses(): Set<KClass<*>>

    fun getRootOf(type: KClass<*>): KClass<*>?

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

    sealed class Type<T : Any>(val type: KClass<T>, val name: ReadMemory) {
        class Sub<T : Any>(type: KClass<T>, name: ReadMemory) : Type<T>(type, name)
        class Root<T : Any>(type: KClass<T>, name: ReadMemory, val subs: List<Sub<out T>>) : Type<T>(type, name)
    }

    class Builder internal constructor(val defaultNameOf: (KClass<*>) -> ReadMemory) {

        internal val roots = ArrayList<Type.Root<*>>()

        fun <T: Any> root(type: KClass<T>, name: ReadMemory = defaultNameOf(type)): Root<T> {
            val root = Root<T>(defaultNameOf)
            roots += Type.Root(type, name, root.subs)
            return root
        }

        inline fun <reified T: Any> root(name: ReadMemory = defaultNameOf(T::class)) = root(T::class, name)

        class Root<T : Any>(val defaultNameOf: (KClass<*>) -> ReadMemory) {

            internal val subs = ArrayList<Type.Sub<out T>>()

            fun <S : T> sub(type: KClass<S>, name: ReadMemory = defaultNameOf(type)) = apply { subs += Type.Sub(type, name) }

            inline fun <reified S: T> sub(name: ReadMemory = defaultNameOf(S::class)) = sub(S::class, name)
        }
    }

    companion object {
        operator fun invoke(defaultNameOf: (KClass<*>) -> ReadMemory = { KBuffer.wrap(simpleTypeAsciiNameOf(it)) }, defaultClassOf: (ReadMemory) -> KClass<*>? = { null }, builder: Builder.() -> Unit = {}): TypeTable = Impl(Builder(defaultNameOf).apply(builder).roots, defaultNameOf, defaultClassOf)
    }
}
