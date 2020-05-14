import org.jetbrains.kotlin.daemon.common.toHexString
import java.security.MessageDigest
import java.util.Properties

plugins {
    id("org.kodein.library.mpp")
}

evaluationDependsOn(":ldb:jni")

val os = System.getProperty("os.name").toLowerCase().let {
    when  {
        "windows" in it -> "windows"
        "mac os x" in it || "darwin" in it || "osx" in it -> "macos"
        "linux" in it -> "linux"
        else -> error("Unknown operating system $it")
    }
}

task("genInfo") {
    val linkRelease = project(":ldb:jni").tasks["linkRelease"]

    dependsOn(linkRelease)

    val outputFile = file("$buildDir/generated/infos/kodein-leveldb-jni-$os.properties")
    outputs.file(outputFile)
    afterEvaluate {
        inputs.files(linkRelease.outputs.files)
    }

    doLast {
        val digest = MessageDigest.getInstance("SHA-1")
        val buf = ByteArray(8192)
        linkRelease.outputs.files
                .filter { it.name.endsWith(".so") || it.name.endsWith(".dylib") || it.name.endsWith(".dll") }
                .first()
                .inputStream()
                .use {
                    while (true) {
                        val n = it.read(buf)
                        if (n == -1) break
                        if (n > 0) digest.update(buf, 0, n)
                    }
                }

        val props = Properties()
        props["version"] = version
        props["sha1"] = digest.digest().toHexString()
        outputFile.outputStream().use {
            props.store(it, null)
        }
    }
}

kodein {
    kotlin {
        add(kodeinTargets.jvm.jvm.copy(name = os)) {
            main.dependencies {
                implementation(project(":ldb:kodein-leveldb"))
            }

            (tasks[mainCompilation.processResourcesTaskName] as ProcessResources).apply {
                dependsOn(
                        project(":ldb:jni").tasks["linkRelease"],
                        tasks["genInfo"]
                )
                from(
                        project(":ldb:jni").tasks["linkRelease"].outputs,
                        tasks["genInfo"].outputs
                )
            }
        }
    }
}

kodeinUpload {
    name = "kodein-leveldb-jni"
    description = "LevelDB library for JVM desktop"
}