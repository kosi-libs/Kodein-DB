package org.kodein.gradle.cmake


class CMakeOptions {
    val defines = HashMap<String, ArrayList<String>>()
    val raw = ArrayList<String>()

    operator fun String.plusAssign(value: String) {
        defines.getOrPut(this) { ArrayList() } .add(value)
    }

    operator fun String.unaryPlus() { this += "1" }

    operator fun String.minusAssign(value: String) {
        raw.addAll(arrayOf("-$this", value))
    }

    operator fun String.unaryMinus() {
        raw.add("-$this")
    }
}
