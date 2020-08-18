package org.kodein.db.leveldb.jvm

public class LevelDBJvmLinuxLoader : AbstractLevelDBLoader("linux") {
    override fun getLibFileName(version: String): String = "libkodein-leveldb-jni-linux-${version.replace('.', '_')}.so"
}
