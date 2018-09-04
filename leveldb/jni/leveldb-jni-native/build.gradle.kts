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
        implementation(project(":leveldb:lib:leveldb-native"))
    }
}

evaluationDependsOn(":leveldb:api:leveldb-api-jvm")
evaluationDependsOn(":leveldb:jni:leveldb-jni")

tasks.create<Exec>("generateJniHeaders") {
    dependsOn(
            project(":leveldb:api:leveldb-api-jvm").tasks["classes"],
            project(":leveldb:jni:leveldb-jni").tasks["classes"]
    )

    val classPath =
            project(":leveldb:api:leveldb-api-jvm").convention.getPluginByName<JavaPluginConvention>("java").sourceSets["main"].output +
            project(":leveldb:jni:leveldb-jni").convention.getPluginByName<JavaPluginConvention>("java").sourceSets["main"].output

    setCommandLine("javah", "-d", "src/main/public/kodein", "-cp", classPath.joinToString(":"), "org.kodein.db.leveldb.jni.LevelDBNative")
}
