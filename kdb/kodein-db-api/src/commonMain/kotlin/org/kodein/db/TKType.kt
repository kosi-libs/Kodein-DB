package org.kodein.db

import kotlin.reflect.KType
import kotlin.reflect.typeOf


@Suppress("unused")
public inline class TKType<T>(public val ktype: KType)

@OptIn(ExperimentalStdlibApi::class)
public inline fun <reified T> tTypeOf(): TKType<T> = TKType(typeOf<T>())
