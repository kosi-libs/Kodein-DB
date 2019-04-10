plugins {
    `cpp-library`
//    `maven-publish`
}

evaluationDependsOn(":ldb:kodein-leveldb-api")
evaluationDependsOn(":ldb:jni:kodein-leveldb-jni-api")
evaluationDependsOn(":ldb:lib:leveldb")

val generation = task<Exec>("generateJniHeaders") {
    group = "build"

    dependsOn(
            project(":ldb:kodein-leveldb-api").tasks["jvmMainClasses"],
            project(":ldb:jni:kodein-leveldb-jni-api").tasks["classes"]
    )

    val ldbApiKotlin = project(":ldb:kodein-leveldb-api").extensions["kotlin"] as org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

    val classPath =
            ldbApiKotlin.targets["jvm"].compilations["main"].output.classesDirs +
                    project(":ldb:jni:kodein-leveldb-jni-api").convention.getPluginByName<JavaPluginConvention>("java").sourceSets["main"].output

    val output = "${buildDir}/nativeHeaders/kodein"

    setCommandLine("javah", "-d", output, "-cp", classPath.joinToString(":"), "org.kodein.db.leveldb.jni.LevelDBJNI")

    outputs.dir(output)
}

val javaHome = System.getProperty("java.home").let { if (it.endsWith("/jre")) file("$it/..").absolutePath else it }
val currentOs = org.gradle.internal.os.OperatingSystem.current()

library {
    privateHeaders {
        from("$javaHome/include")
        from("${project(":ldb:lib:leveldb").buildDir}/out/include")
    }

    publicHeaders {
        from("$buildDir/nativeHeaders")
    }

    if (currentOs.isLinux()) {
        privateHeaders {
            from("$javaHome/include/linux")
        }
    }

    binaries.configureEach {
        compileTask.get().dependsOn(generation)
        compileTask.get().dependsOn(project(":ldb:lib:leveldb").tasks["build"])

        if (this is CppSharedLibrary) {
            linkTask.get().linkerArgs.addAll(
                    "-L${project(":ldb:lib:snappy").buildDir}/out/lib",
                    "-L${project(":ldb:lib:crc32c").buildDir}/out/lib",
                    "-L${project(":ldb:lib:leveldb").buildDir}/out/lib",
                    "-lleveldb", "-lsnappy", "-lcrc32c"
            )
        }
    }

//    dependencies {
//        api(project(":ldb:lib:leveldb"))
//    }
}

//apply(from = rootProject.file("gradle/toolchains.gradle"))

if (currentOs.isLinux()) {
    tasks.withType<CppCompile> {
        macros.put("_GLIBCXX_USE_CXX11_ABI", "0")
    }
}
