package org.kodein.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.util.*
import kotlin.collections.HashMap


class androidNdkPlugin : Plugin<Project> {

    class Extension(private val rootProject: Project) {
        fun ndkPath(version: String): String {
            (rootProject.findProperty("android-ndk-$version-path") as? String)?.let { return it }

            val localPropsFile = rootProject.file("local.properties")
            check(localPropsFile.exists()) { "Please create android root local.properties" }

            val localProps = localPropsFile.inputStream().use { Properties().apply { load(it) } }

            val sdkDir = localProps.getProperty("sdk.dir")?.let { File(it) }
                ?: throw IllegalStateException("Please set sdk.dir android sdk path in root local.properties")

            val ndkDir = sdkDir.resolve("ndk").resolve(version).takeIf { it.exists() }
                ?: throw IllegalStateException("Please install Android NDK version $version")

            val ndkPath = ndkDir.absolutePath
            @Suppress("UNCHECKED_CAST")
            (rootProject.properties as HashMap<String, Any>).put("android-ndk-$version-path", ndkPath)

            return ndkPath
        }
    }

    override fun apply(target: Project) {
        target.extensions.add("androidNdk", Extension(target.rootProject))
    }
}
