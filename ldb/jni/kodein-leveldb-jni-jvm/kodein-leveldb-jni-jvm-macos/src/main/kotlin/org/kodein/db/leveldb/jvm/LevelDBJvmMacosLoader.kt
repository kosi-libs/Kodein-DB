package org.kodein.db.leveldb.jvm

public class LevelDBJvmMacosLoader : AbstractLevelDBJvmLoader(
    osName = "macos",
    libExtension = "dylib",
    jniDefaultLocation = "${System.getProperty("user.home")}/Library/Caches/Kodein-DB"
)
