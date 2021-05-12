package org.kodein.db.encryption

import org.kodein.db.Options
import org.kodein.db.model.*
import org.kodein.memory.io.ReadMemory
import kotlin.reflect.KClass


public sealed class EncryptOptions {
    public class Encrypt(
        internal val key: ReadMemory,
        internal val hashDocumentID: Boolean = true,
        internal val hashIndexValues: Indexes = Indexes.All,
        internal val encryptIndexMetadata: Indexes = Indexes.All
    ) : EncryptOptions()

    public object KeepPlain : EncryptOptions()

    public sealed class Indexes {
        public object All: Indexes()
        public object None : Indexes()
        public class Only(internal vararg val names: String): Indexes()
        public class AllBut(internal vararg val names: String): Indexes()
    }
}

internal operator fun EncryptOptions.Indexes.contains(name: String): Boolean =
    when(this) {
        EncryptOptions.Indexes.All -> true
        EncryptOptions.Indexes.None -> false
        is EncryptOptions.Indexes.Only -> name in names
        is EncryptOptions.Indexes.AllBut -> name !in names
    }

public class EncryptedDocumentKey(internal val key: ReadMemory) : Options.Get, Options.Find, Options.Puts

// Only for test!
internal class IV(val iv: ReadMemory): Options.Puts
