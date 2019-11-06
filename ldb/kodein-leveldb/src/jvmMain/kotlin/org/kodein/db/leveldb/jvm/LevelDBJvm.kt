package org.kodein.db.leveldb.jvm

import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.jni.LevelDBJNI
import org.kodein.memory.text.toHexString
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*


object LevelDBJvm : LevelDBFactory by LevelDBJNI.Factory {
    init {
        val resourcesProps = javaClass.getResourceAsStream("/info.properties").use { Properties().apply { load(it) } }
        val os = resourcesProps["os"]!!
        val version = resourcesProps["version"]!!
        val sha1 = resourcesProps["sha1"]!!

        val ext = if ("mac" in System.getProperty("os.name").toLowerCase()) "dylib" else "so"

        val libname = "libkodein-leveldb-jni-$os-$version.$ext"

        val location = Paths.get(System.getProperty("org.kodein.db.leveldb.jvm.jni-location") ?: "${System.getProperty("user.home")}/.kodein-db/$os/$version").toAbsolutePath()
        val info = location.resolve("info.properties")
        val lib = location.resolve(libname)

        val extract =
                if (Files.exists(info) && Files.exists(lib)) {
                    val existingProps = Files.newInputStream(info).use { Properties().apply { load(it) } }
                    existingProps["sha1"] != sha1
                } else {
                    true
                }

        if (extract) {
            Files.createDirectories(location)

            javaClass.getResourceAsStream("/$libname").use { input ->
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

            if (hex != sha1)
                throw IllegalStateException("Extraction failed or library was tampered with.")

            Files.newOutputStream(info).use {
                resourcesProps.store(it, null)
            }
        }

        System.load(lib.toAbsolutePath().toAbsolutePath().toString())
    }
}
