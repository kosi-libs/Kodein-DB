plugins {
    id("org.kodein.library.jvm")
}

dependencies {
    implementation(project(":ldb:kodein-leveldb"))
}

val currentOs = org.gradle.internal.os.OperatingSystem.current()

val copyResources by tasks.creating(Sync::class) {
    onlyIf { currentOs.isMacOsX }
    if (currentOs.isMacOsX) {
        dependsOn(
            ":ldb:jni:c:buildKodein-leveldb-jni-macos",
            ":ldb:jni:c:genInfoProperties-macos"
        )
    }
    from(rootDir.resolve("ldb/jni/c/build/cmake/out/kodein-leveldb-jni-macos/lib/libkodein-leveldb-jni.dylib"))
    from(rootDir.resolve("ldb/jni/c/build/generated/kodein-leveldb-jni-macos/kodein-leveldb-jni.properties"))
    into(buildDir.resolve("jniResources/org/kodein/db/leveldb/jvm/jni/macos"))
}

tasks.getByName<ProcessResources>(kotlin.target.compilations["main"].processResourcesTaskName) {
    onlyIf { currentOs.isMacOsX }
    dependsOn(copyResources)
    from(buildDir.resolve("jniResources"))
}

kodeinUpload {
    name = "kodein-leveldb-jni-jvm-macos"
    description = "LevelDB native library for MacOS JVM"
}

tasks.withType<PublishToMavenRepository>().all {
    onlyIf { currentOs.isMacOsX }
    if (currentOs.isMacOsX) tasks["hostOnlyPublish"].dependsOn(this)
}
