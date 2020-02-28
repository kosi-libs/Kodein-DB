package org.kodein.db.leveldb.jvm

class LevelDBJvmLinuxLoader : AbstractLevelDBLoader("linux") {
    override fun getLibFileName(version: String): String = "libkodein-leveldb-jni-linux-$version.so"
}
