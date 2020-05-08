package net.kodein.db.plugin.fts.impl

import net.kodein.db.plugin.fts.RegionsMap
import net.kodein.db.plugin.fts.Stemmer

internal class TransformsImpl : Stemmer.Transforms, StepImpl {

    class ExecImpl : Stemmer.Transforms.Exec {
        override lateinit var R: Map<String, Int>
        var ret: StepImpl.Return? = null

        override fun to(token: String, nextStep: String?) {
            check(ret == null) { "to() has already been called" }
            ret = StepImpl.Return(token, nextStep)
        }
    }

    private val exec = ExecImpl()

    var transformation: (Stemmer.Transforms.Exec.(String) -> Unit)? = null

    override fun exec(transform: Stemmer.Transforms.Exec.(String) -> Unit) {
        require(transformation == null) { "Transformation has already been set" }
        transformation = transform
    }

    override fun execute(token: String, regions: RegionsMap): StepImpl.Return {
        val transform = transformation ?: return StepImpl.Return(token, "")
        exec.R = regions
        exec.ret = null
        transform(exec, token)
        return exec.ret ?: StepImpl.Return(token, "")
    }
}
