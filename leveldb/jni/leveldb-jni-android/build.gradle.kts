plugins {
    id("kodein-android")
}

dependencies {
    compile(project(":leveldb:jni:leveldb-jni"))
}

android {
    externalNativeBuild {
        cmake {
            setPath("CMakeLists.txt")
        }
    }
}
