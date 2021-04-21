package org.kodein.db.leveldb.jvm

public class LevelDBJvmWindowsLoader : AbstractLevelDBJvmLoader(
    osName = "windows",
    libPrefix = "",
    libSuffix = "dll",
    jniDefaultLocation = (System.getenv("LOCALAPPDATA") ?: "${System.getProperty("user.home")}/AppData/Local") + "/Kodein-DB"
)
