plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
}

gradlePlugin{
    plugins {
        register("android-ndk-plugin") {
            id = "org.kodein.gradle.android-ndk"
            implementationClass = "org.kodein.gradle.androidNdkPlugin"
        }
        register("cmake-plugin") {
            id = "org.kodein.gradle.cmake"
            implementationClass = "org.kodein.gradle.cmake.CMakePlugin"
        }
    }
}
