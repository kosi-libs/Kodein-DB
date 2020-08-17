plugins {
    id("org.kodein.mpp-with-android")
}

val kodeinLogVer: String by rootProject.extra
val kodeinMemoryVer: String by rootProject.extra

repositories {
    mavenLocal()
    google()
    maven(url = "https://kotlin.bintray.com/kotlinx")
    maven(url = "https://dl.bintray.com/kodein-framework/kodein-dev")
    jcenter()
}

kodein {
    kotlin {
        common.main.dependencies {
            api("org.kodein.log:kodein-log:$kodeinLogVer")
            api("org.kodein.memory:kodein-memory:$kodeinMemoryVer")
            api("org.kodein.memory:kodein-file:$kodeinMemoryVer")
            api(project(":ldb:kodein-leveldb-api"))

            api("org.jetbrains.kotlin:kotlin-test-common")
            api("org.jetbrains.kotlin:kotlin-test-annotations-common")
        }

        add(kodeinTargets.jvm.jvm) {
            target.setCompileClasspath()

            main.dependencies {
                api("org.jetbrains.kotlin:kotlin-test")
                api("org.jetbrains.kotlin:kotlin-test-junit")
                api("junit:junit:4.12")
            }
        }

        add(kodeinTargets.jvm.android) {
            target {
                publishLibraryVariants = emptyList()
            }
            main.dependencies {
                api("org.jetbrains.kotlin:kotlin-test")
                api("org.jetbrains.kotlin:kotlin-test-junit")
                api("androidx.test.ext:junit:1.1.1")
                api("androidx.test.espresso:espresso-core:3.2.0")
            }
        }

        add(kodeinTargets.native.allDarwin + kodeinTargets.native.allDesktop)

        allTargets {
            mainCommonCompilation.kotlinOptions.freeCompilerArgs = listOf("-Xuse-experimental=kotlin.Experimental")
        }
    }
}
