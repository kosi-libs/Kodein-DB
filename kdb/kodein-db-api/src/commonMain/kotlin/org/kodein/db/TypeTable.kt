package org.kodein.db

import kotlin.reflect.KClass

interface TypeTable {

    fun getTypeName(type: KClass<*>): String

    fun getTypeClass(name: String): KClass<*>?

    fun getRegisteredClasses(): Set<KClass<*>>

    fun getRootOf(type: KClass<*>): KClass<*>?

    private class Impl(roots: Iterable<Type.Root<*>>, val defaultNameOf: (KClass<*>) -> String, val defaultClassOf: (String) -> KClass<*>?) : TypeTable {

        private val byClass = HashMap<KClass<*>, Type<*>>()

        private val byName = HashMap<String, Type<*>>()

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

        override fun getTypeClass(name: String): KClass<*>? = byName[name]?.type ?: defaultClassOf(name)

        override fun getRegisteredClasses(): Set<KClass<*>> = byClass.keys

        override fun getRootOf(type: KClass<*>): KClass<*>? = roots[type]?.type

    }

    sealed class Type<T : Any>(val type: KClass<T>, val name: String, val oldNames: Set<String>) {
        class Sub<T : Any>(type: KClass<T>, name: String, oldNames: Set<String>) : Type<T>(type, name, oldNames)
        class Root<T : Any>(type: KClass<T>, name: String, oldNames: Set<String>, val subs: List<Sub<out T>>) : Type<T>(type, name, oldNames)
    }

    class Builder internal constructor(val defaultNameOf: (KClass<*>) -> String) {

        internal val roots = ArrayList<Type.Root<*>>()

        fun <T: Any> root(type: KClass<T>, name: String = defaultNameOf(type), oldNames: Set<String> = emptySet()): Root<T> {
            val root = Root<T>(defaultNameOf)
            roots += Type.Root(type, name, oldNames, root.subs)
            return root
        }

        inline fun <reified T: Any> root(name: String = defaultNameOf(T::class), oldNames: Set<String> = emptySet()) = root(T::class, name, oldNames)

        class Root<T : Any>(val defaultNameOf: (KClass<*>) -> String) {

            internal val subs = ArrayList<Type.Sub<out T>>()

            fun <S : T> sub(type: KClass<S>, name: String = defaultNameOf(type), oldNames: Set<String> = emptySet()) = apply { subs += Type.Sub(type, name, oldNames) }

            inline fun <reified S: T> sub(name: String = defaultNameOf(S::class), oldNames: Set<String> = emptySet()) = sub(S::class, name, oldNames)
        }
    }

    companion object {
        operator fun invoke(defaultNameOf: (KClass<*>) -> String = ::simpleTypeNameOf, defaultClassOf: (String) -> KClass<*>? = { null }, builder: Builder.() -> Unit = {}): TypeTable = Impl(Builder(defaultNameOf).apply(builder).roots, defaultNameOf, defaultClassOf)
    }
}
