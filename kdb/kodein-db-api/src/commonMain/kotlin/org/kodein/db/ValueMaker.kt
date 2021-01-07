package org.kodein.db


public interface ValueMaker {

    public fun valueOf(value: Any): Value

    public fun valueOfAll(vararg values: Any): Value

}
