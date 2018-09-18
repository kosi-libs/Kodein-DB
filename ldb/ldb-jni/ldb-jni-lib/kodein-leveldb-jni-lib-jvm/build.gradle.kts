import org.gradle.internal.os.OperatingSystem

plugins {
    `cpp-library`
    `maven-publish`
}

val javaHome = System.getProperty("java.home").let { if (it.endsWith("/jre")) file("$it/..").absolutePath else it }

library {
    privateHeaders {
        from("$javaHome/include")
    }

    if (OperatingSystem.current().isLinux()) {
        privateHeaders {
            from("$javaHome/include/linux")
        }
    }

    dependencies {
        api(project(":ldb:ldb-lib:leveldb"))
    }
}

apply(from = rootProject.file("gradle/toolchains.gradle"))

evaluationDependsOn(":ldb:ldb-api:kodein-leveldb-api-jvm")
evaluationDependsOn(":ldb:ldb-jni:kodein-leveldb-jni")

task<Exec>("generateJniHeaders") {
    dependsOn(
            project(":ldb:ldb-api:kodein-leveldb-api-jvm").tasks["classes"],
            project(":ldb:ldb-jni:kodein-leveldb-jni").tasks["classes"]
    )

    val classPath =
            project(":ldb:ldb-api:kodein-leveldb-api-jvm").convention.getPluginByName<JavaPluginConvention>("java").sourceSets["main"].output +
            project(":ldb:ldb-jni:kodein-leveldb-jni").convention.getPluginByName<JavaPluginConvention>("java").sourceSets["main"].output

    setCommandLine("javah", "-d", "src/main/public/kodein", "-cp", classPath.joinToString(":"), "org.kodein.db.leveldb.jni.LevelDBJNI")
}

if (OperatingSystem.current().isLinux()) {
    tasks.withType<CppCompile> {
        macros.put("_GLIBCXX_USE_CXX11_ABI", "0")
    }
}
