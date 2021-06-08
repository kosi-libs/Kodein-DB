package org.kodein.db.impl.model.cache

public class ModelMutatedException(model: Any): IllegalStateException(
    "Hash code of cached model has changed, which probably means that the model was mutated while in cache.\n" +
    "If you are sure of what you are doing, you can disable this check with the open option ModelCache.NoHashCodeImmutabilityChecks,\n" +
    "or disable the cache entirely with ModelCache.Disable.\n" +
    "Model (${model::class.qualifiedName}): $model"
)