plugins {
    id("org.kodein.library.jvm")
}

dependencies {
    implementation(project(":ldb:kodein-leveldb"))
}

val currentOs = org.gradle.internal.os.OperatingSystem.current()

val copyResources by tasks.creating(Sync::class) {
    onlyIf { currentOs.isLinux }
    if (currentOs.isLinux) {
        dependsOn(
            ":ldb:jni:c:buildKodein-leveldb-jni-linux",
            ":ldb:jni:c:genInfoProperties-linux"
        )
    }
    from(rootDir.resolve("ldb/jni/c/build/cmake/out/kodein-leveldb-jni-linux/lib/libkodein-leveldb-jni.so"))
    from(rootDir.resolve("ldb/jni/c/build/generated/kodein-leveldb-jni-linux/kodein-leveldb-jni.properties"))
    into(buildDir.resolve("jniResources/org/kodein/db/leveldb/jvm/jni/linux"))
}

tasks.getByName<ProcessResources>(kotlin.target.compilations["main"].processResourcesTaskName) {
    onlyIf { currentOs.isLinux }
    dependsOn(copyResources)
    from(buildDir.resolve("jniResources"))
}

kodeinUpload {
    name = "kodein-leveldb-jni-jvm-linux"
    description = "LevelDB native library for Linux JVM"
}

tasks.withType<PublishToMavenRepository>().all {
    onlyIf { currentOs.isLinux }
    if (currentOs.isLinux) tasks["hostOnlyPublish"].dependsOn(this)
}
