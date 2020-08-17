package org.kodein.db.leveldb.jvm

import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.jni.LevelDBJNI
import org.kodein.memory.text.toHexString
import java.lang.IllegalStateException
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*


public object LevelDBJvm : LevelDBFactory by LevelDBJNI.Factory {
    init {
        val os = System.getProperty("os.name").toLowerCase().let {
            when  {
                "windows" in it -> "windows"
                "mac os x" in it || "darwin" in it || "osx" in it -> "macos"
                "linux" in it -> "linux"
                else -> error("Unknown operating system $it")
            }
        }

        val loader = try {
            Class.forName("org.kodein.db.leveldb.jvm.LevelDBJvm${os.capitalize()}Loader")
                    .getDeclaredConstructor()
                    .newInstance()
                    as AbstractLevelDBLoader
        } catch (ex: Throwable) {
            throw IllegalStateException("Could not load kodein-leveldb-jni-$os. Have you added it to the classpath?", ex)
        }

        loader.load()
    }
}
