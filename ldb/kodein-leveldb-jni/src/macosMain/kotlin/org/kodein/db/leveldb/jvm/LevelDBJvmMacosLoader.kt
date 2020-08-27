package org.kodein.db.leveldb.jvm

public class LevelDBJvmMacosLoader : AbstractLevelDBLoader("macos") {
    override fun getLibFileName(version: String): String = "libkodein-leveldb-jni-macos-${version.replace('.', '_')}.dylib"
}
