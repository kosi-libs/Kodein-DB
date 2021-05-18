package org.kodein.db

import org.kodein.db.model.ModelIndexData


public class IndexValues(values: Iterable<Any>) : ModelIndexData(values.map { it to null })
