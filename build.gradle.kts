plugins {
    id("org.kodein.root")
}

val kotlinxAtomicFuVer by extra { "0.14.3-1.4.0-rc" } // CAUTION: also change in buildscript!
val kotlinxSerializationVer by extra { "1.0-M1-1.4.0-rc" }
//val kotlinxCoroutinesVer by extra { "1.3.3" }
val kodeinLogVer by extra { "0.4.0-kotlin-1.4-rc-43" }
val kodeinMemoryVer by extra { "0.2.0-kotlin-1.4-rc-35" }

buildscript {
    repositories {
        maven(url = "https://kotlin.bintray.com/kotlinx")
    }

    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.14.3-1.4.0-rc")
    }
}

allprojects {
    group = "org.kodein.db"
    version = "0.2.0"

    repositories {
        mavenLocal()
        google()
        maven(url = "https://kotlin.bintray.com/kotlinx")
        maven(url = "https://dl.bintray.com/kodein-framework/kodein-dev")
        jcenter()
    }
}

kodeinPublications {
    repo = "Kodein-DB"
}

// see https://github.com/gradle/kotlin-dsl/issues/607#issuecomment-375687119
subprojects { parent!!.path.takeIf { it != rootProject.path }?.let { evaluationDependsOn(it)  } }
