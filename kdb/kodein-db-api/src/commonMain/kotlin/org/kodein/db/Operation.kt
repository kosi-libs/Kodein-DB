package org.kodein.db


public sealed class Operation<M : Any> {

    public abstract val options: List<Options.Listeners>

    public abstract val key: Key<M>

    public class Put<M : Any>(
        override val options: List<Options.Listeners>,
        override val key: Key<M>,
        public val model: M
    ) : Operation<M>()

    public class Delete<M : Any>(
        override val options: List<Options.Listeners>,
        override val key: Key<M>,
        getModel: () -> M?
    ) : Operation<M>() {
        private val model: M? by lazy(getModel)
        public fun model(): M? = model
    }
}
