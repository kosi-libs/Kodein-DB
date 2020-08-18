package org.kodein.db.leveldb.jvm

public class LevelDBJvmWindowsLoader : AbstractLevelDBLoader("windows") {
    override fun getLibFileName(version: String): String = "kodein-leveldb-jni-windows-${version.replace('.', '_')}.dll"
}
