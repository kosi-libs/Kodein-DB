package org.kodein.db.leveldb.jvm

import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.jni.LevelDBJNI
import org.kodein.memory.text.toHex
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*


public abstract class AbstractLevelDBJvmLoader(
    private val osName: String,
    private val libExtension: String,
    private val jniDefaultLocation: String
) : LevelDBFactory by LevelDBJNI.Factory {

    private fun sha1Of(path: Path): String {
        val digest = MessageDigest.getInstance("SHA-1")
        Files.newInputStream(path).use {
            val buf = ByteArray(8192)
            while (true) {
                val n = it.read(buf)
                if (n == -1) break
                if (n > 0) digest.update(buf, 0, n)
            }
            return digest.digest().toHex()
        }
    }

    public fun load() {
        val resourcesProps = javaClass.getResourceAsStream("jni/$osName/kodein-leveldb-jni.properties")
                ?.use { Properties().apply { load(it) } }
                ?: error("Could not load jni/$osName/kodein-leveldb-jni.properties")
        val version = resourcesProps.getProperty("version")!!
        val expectedSha1 = resourcesProps.getProperty("sha1")!!

        val location = Paths.get(System.getProperty("org.kodein.db.leveldb.jvm.jni.location") ?: jniDefaultLocation)
            .resolve(version)
            .toAbsolutePath()
        val propsPath = location.resolve("kodein-leveldb-jni.properties")
        val props = propsPath.takeIf { Files.exists(it) }
            ?.let { Files.newInputStream(it) }
            ?.use { Properties().apply { load(it) } }
        val libFileName = "libkodein-leveldb-jni.$libExtension"
        val libPath = location.resolve(libFileName)

        val safeChecks = System.getProperty("org.kodein.db.leveldb.jvm.jni.safe-checks") == "true"

        val extract =
                if (props != null && Files.exists(libPath)) {
                    if (props["sha1"] != expectedSha1) true
                    else safeChecks && sha1Of(libPath) != expectedSha1
                } else true

        if (extract) {
            Files.createDirectories(location)

            javaClass.getResourceAsStream("jni/$osName/$libFileName").use { input ->
                check(input != null) { "Did not find jni/$osName/$libFileName" }
                Files.newOutputStream(libPath).use { output ->
                    input.copyTo(output)
                }
            }

            if (safeChecks) {
                check(sha1Of(libPath) == expectedSha1) { "Extraction failed or library was tampered with." }
            }

            Files.newOutputStream(propsPath).use {
                resourcesProps.store(it, null)
            }
        }

        @Suppress("UnsafeDynamicallyLoadedCode")
        System.load(libPath.toAbsolutePath().toString())
    }
}
