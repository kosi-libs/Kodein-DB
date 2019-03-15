plugins {
    `cpp-library`
    `maven-publish`
}

val javaHome = System.getProperty("java.home").let { if (it.endsWith("/jre")) file("$it/..").absolutePath else it }
val currentOs = org.gradle.internal.os.OperatingSystem.current()

library {
    privateHeaders {
        from("$javaHome/include")
    }

    if (currentOs.isLinux()) {
        privateHeaders {
            from("$javaHome/include/linux")
        }
    }

    dependencies {
        api(project(":ldb:ldb-lib:leveldb"))
    }
}

apply(from = rootProject.file("gradle/toolchains.gradle"))

evaluationDependsOn(":ldb:kodein-leveldb-api")
evaluationDependsOn(":ldb:ldb-jni:kodein-leveldb-jni")

task<Exec>("generateJniHeaders") {
    dependsOn(
            project(":ldb:kodein-leveldb-api").tasks["jvmMainClasses"],
            project(":ldb:ldb-jni:kodein-leveldb-jni").tasks["classes"]
    )

    val ldbApiKotlin = project(":ldb:kodein-leveldb-api").extensions["kotlin"] as org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

    val classPath =
            ldbApiKotlin.targets["jvm"].compilations["main"].output.classesDirs +
            project(":ldb:ldb-jni:kodein-leveldb-jni").convention.getPluginByName<JavaPluginConvention>("java").sourceSets["main"].output

    setCommandLine("javah", "-d", "src/main/public/kodein", "-cp", classPath.joinToString(":"), "org.kodein.db.leveldb.jni.LevelDBJNI")
}

if (currentOs.isLinux()) {
    tasks.withType<CppCompile> {
        macros.put("_GLIBCXX_USE_CXX11_ABI", "0")
    }
}
