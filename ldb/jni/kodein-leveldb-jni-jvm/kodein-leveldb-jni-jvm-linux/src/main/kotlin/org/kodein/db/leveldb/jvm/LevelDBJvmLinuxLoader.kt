package org.kodein.db.leveldb.jvm

public class LevelDBJvmLinuxLoader : AbstractLevelDBJvmLoader(
    osName = "linux",
    libExtension = "so",
    jniDefaultLocation = "${System.getProperty("user.home")}/.cache/Kodein-DB"
)
