plugins {
    id("kodein-android")
}

dependencies {
    compile(project(":ldb:ldb-jni:kodein-leveldb-jni"))
}

android {
    externalNativeBuild {
        cmake {
            setPath("CMakeLists.txt")
        }
    }
}
