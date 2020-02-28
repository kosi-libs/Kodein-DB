package org.kodein.db.leveldb.jvm

class LevelDBJvmMacosLoader : AbstractLevelDBLoader("macos") {
    override fun getLibFileName(version: String): String = "libkodein-leveldb-jni-macos-$version.dylib"
}
