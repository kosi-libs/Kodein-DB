plugins {
    id("org.kodein.root")
}

buildscript {
    repositories {
        maven(url = "https://kotlin.bintray.com/kotlinx")
    }

    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.15.1")
    }
}

allprojects {
    group = "org.kodein.db"
    version = "0.4.2-beta"

    repositories {
        mavenLocal()
        google()
        maven(url = "https://kotlin.bintray.com/kotlinx")
        maven(url = "https://dl.bintray.com/kodein-framework/kodein-dev")
        maven(url = "https://dl.bintray.com/kodein-framework/Kodein-Memory")
        jcenter()
    }
}

val kotlinxAtomicFuVer by extra { "0.15.1" } // CAUTION: also change in buildscript!
val kotlinxSerializationVer by extra { "1.0.1" }
val kodeinLogVer by extra { "0.8.0" }
val kodeinMemoryVer by extra { "0.5.0" }

val androidNdkVer by extra { "21.0.6113669" } // CAUTION: also change in CI workflows!

val currentOs = org.gradle.internal.os.OperatingSystem.current()!!

when {
    currentOs.isWindows -> {
        extra["osName"] = "windows"
        extra["libExt"] = "dll"
    }
    currentOs.isMacOsX -> {
        extra["osName"] = "macos"
        extra["libExt"] = "dylib"
    }
    currentOs.isLinux -> {
        extra["osName"] = "linux"
        extra["libExt"] = "so"
    }
    else -> error("Unknown operating system ${currentOs.name}")
}


kodeinPublications {
    repo = "Kodein-DB"
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
