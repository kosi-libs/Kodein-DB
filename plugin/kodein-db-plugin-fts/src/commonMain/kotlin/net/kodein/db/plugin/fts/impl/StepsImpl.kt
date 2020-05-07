package net.kodein.db.plugin.fts.impl

import net.kodein.db.plugin.fts.Stemmer

class StepsImpl(val accents: Pair<String, String>?) : Stemmer.Steps {
    private val steps = HashMap<Int, StepImpl>()

    override var firstStep: Int = 0

    override fun Int.invoke(builder: Stemmer.Step.() -> Stemmer.Step.NextStep): Int {
        val step = StepImpl(accents).apply { nextStep = builder() }
        steps[this] = step
        return this
    }

    @Suppress("NAME_SHADOWING")
    fun run(token: String, regions: Map<Int, Int>): String {
        var token = token
        var stepNbr = firstStep
        while (stepNbr != Int.MAX_VALUE) {
            val step = steps[stepNbr] ?: error("Step $stepNbr is not defined")
            val (nextToken, nextStepNbr) = step.execute(token, regions)
            token = nextToken
            stepNbr = nextStepNbr
        }
        return token
    }
}
