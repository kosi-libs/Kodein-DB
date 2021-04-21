package org.kodein.db.leveldb.jvm

public class LevelDBJvmLinuxLoader : AbstractLevelDBJvmLoader(
    osName = "linux",
    libPrefix = "lib",
    libSuffix = "so",
    jniDefaultLocation = "${System.getProperty("user.home")}/.cache/Kodein-DB"
)
