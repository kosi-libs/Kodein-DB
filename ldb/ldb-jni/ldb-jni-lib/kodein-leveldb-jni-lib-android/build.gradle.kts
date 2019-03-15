plugins {
    id("org.kodein.library.android")
}

dependencies {
    api(project(":ldb:ldb-jni:kodein-leveldb-jni"))
}

android {
    externalNativeBuild {
        cmake {
            setPath("CMakeLists.txt")
        }
    }
}
