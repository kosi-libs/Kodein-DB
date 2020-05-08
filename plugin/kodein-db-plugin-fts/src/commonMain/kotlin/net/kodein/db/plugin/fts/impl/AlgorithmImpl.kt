package net.kodein.db.plugin.fts.impl

import net.kodein.db.plugin.fts.AccentsMap
import net.kodein.db.plugin.fts.RegionsMap
import net.kodein.db.plugin.fts.Stemmer

internal class AlgorithmImpl(private val accentsMap: AccentsMap?) : Stemmer.Algorithm, StepImpl {
    override var firstStep: String = ""

    private val steps = HashMap<String, Pair<StepImpl, Stemmer.Algorithm.Go>>()

    private var regions: RegionsImpl? = null

    override fun regions(builder: Stemmer.Regions.() -> Unit) {
        regions = RegionsImpl().apply(builder)
    }

    private inline fun <T: StepImpl> String.step(builder: T.() -> Stemmer.Algorithm.Go, new: () -> T): String {
        val step = new()
        val go = step.run { builder() }
        steps[this] = step to go
        return this
    }

    override fun String.changes(builder: Stemmer.Changes.() -> Stemmer.Algorithm.Go): String =
            step(builder) { ChangesImpl() }

    override fun String.executes(builder: Stemmer.Algorithm.() -> Stemmer.Algorithm.Go): String =
            step(builder) { AlgorithmImpl(accentsMap) }

    override fun String.searches(builder: Stemmer.Searches.() -> Stemmer.Algorithm.Go): String =
            step(builder) { SearchesImpl(accentsMap) }

    override fun String.transforms(builder: Stemmer.Transforms.() -> Stemmer.Algorithm.Go): String =
            step(builder) { TransformsImpl() }

    override fun execute(token: String, regions: RegionsMap): StepImpl.Return {
        val thisRegions = this.regions?.findFor(token) ?: regions
        var currentToken = token
        var currentStepName: String? = firstStep.takeIf { it.isNotEmpty() } ?: error("firstStep is not defined")
        while (currentStepName != null) {
            val (currentStep, nextGo) = steps[currentStepName] ?: error("Step $currentStepName is not defined")
            val (nextToken, nextStepName) = currentStep.execute(currentToken, thisRegions)
            if (nextStepName == null) break
            currentStepName = nextStepName.takeIf { it.isNotEmpty() } ?: if (currentToken != nextToken) nextGo.changed else nextGo.noChange
            currentToken = nextToken
        }
        return StepImpl.Return(currentToken, "")
    }

}
