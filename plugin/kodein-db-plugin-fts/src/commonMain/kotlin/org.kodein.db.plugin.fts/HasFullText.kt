package org.kodein.db.plugin.fts

import org.kodein.memory.io.ReadBuffer


public typealias FtsTokens = Map<String, ReadBuffer>
public typealias FtsTexts = Map<String, FtsTokens>

public interface HasFullText {
    public fun texts(): FtsTexts
}
