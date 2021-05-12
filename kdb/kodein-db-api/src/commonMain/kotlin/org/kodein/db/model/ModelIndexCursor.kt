package org.kodein.db.model

import org.kodein.db.Options
import kotlin.reflect.KClass


public interface ModelIndexCursor<M : Any> : ModelCursor<M> {

    public fun <T : Any> associatedObject(type: KClass<T>, vararg options: Options.Get): T?

}

public inline fun <reified T : Any> ModelIndexCursor<*>.associatedObject(vararg options: Options.Get): T? = associatedObject(T::class, *options)
