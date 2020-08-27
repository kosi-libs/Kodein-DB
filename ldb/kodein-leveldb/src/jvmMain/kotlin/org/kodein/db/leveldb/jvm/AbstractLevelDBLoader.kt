package org.kodein.db.leveldb.jvm

import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.jni.LevelDBJNI
import org.kodein.memory.text.toHexString
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*


public abstract class AbstractLevelDBLoader(public val os: String) : LevelDBFactory by LevelDBJNI.Factory {

    public abstract fun getLibFileName(version: String): String

    public fun load() {
        val resourcesProps = javaClass.getResourceAsStream("/kodein-leveldb-jni-$os.properties")
                ?.use { Properties().apply { load(it) } }
                ?: error("Could not load kodein-leveldb-jni-$os.properties")
        val version = resourcesProps.getProperty("version")!!
        val sha1 = resourcesProps.getProperty("sha1")!!

        val location = Paths.get(System.getProperty("org.kodein.db.leveldb.jvm.jni-location") ?: "${System.getProperty("user.home")}/.kodein-db/$os/$version").toAbsolutePath()
        val info = location.resolve("info.properties")
        val libFileName = getLibFileName(version)
        val lib = location.resolve(libFileName)

        val extract =
                if (Files.exists(info) && Files.exists(lib)) {
                    val existingProps = Files.newInputStream(info).use { Properties().apply { load(it) } }
                    existingProps["sha1"] != sha1
                } else {
                    true
                }

        if (extract) {
            Files.createDirectories(location)

            javaClass.getResourceAsStream("/$libFileName").use { input ->
                check(input != null) { "Did not find /$libFileName" }
                Files.newOutputStream(lib).use { output ->
                    input.copyTo(output)
                }
            }

            val digest = MessageDigest.getInstance("SHA-1")
            val hex = Files.newInputStream(lib).use {
                val buf = ByteArray(8192)
                while (true) {
                    val n = it.read(buf)
                    if (n == -1) break
                    if (n > 0) digest.update(buf, 0, n)
                }
                digest.digest().toHexString()
            }

            check(hex == sha1) { "Extraction failed or library was tampered with." }

            Files.newOutputStream(info).use {
                resourcesProps.store(it, null)
            }
        }

        @Suppress("UnsafeDynamicallyLoadedCode")
        System.load(lib.toAbsolutePath().toString())
    }
}
