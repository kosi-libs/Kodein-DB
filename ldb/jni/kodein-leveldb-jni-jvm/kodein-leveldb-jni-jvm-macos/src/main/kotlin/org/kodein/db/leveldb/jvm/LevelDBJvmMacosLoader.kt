package org.kodein.db.leveldb.jvm

public class LevelDBJvmMacosLoader : AbstractLevelDBJvmLoader(
    osName = "macos",
    libPrefix = "lib",
    libSuffix = "dylib",
    jniDefaultLocation = "${System.getProperty("user.home")}/Library/Caches/Kodein-DB"
)
