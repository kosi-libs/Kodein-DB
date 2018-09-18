import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.konan.target.linker

plugins {
    `cpp-library`
}

library {
    linkage.set(listOf(Linkage.STATIC))

    source.from(
            fileTree(project(":ldb:ldb-lib").file("src/leveldb/db")) { exclude("*_test.cc", "leveldb_main.cc") },
            fileTree(project(":ldb:ldb-lib").file("src/leveldb/table")) { exclude("*_test.cc") },
            fileTree(project(":ldb:ldb-lib").file("src/leveldb/util")) { exclude("*_test.cc") },
            fileTree(project(":ldb:ldb-lib").file("src/snappy"))
    )

    privateHeaders.from(
            project(":ldb:ldb-lib").file("src/snappy"),
            project(":ldb:ldb-lib").file("src/leveldb")
    )

    publicHeaders.from(project(":ldb:ldb-lib").file("src/leveldb/include"))
}

apply(from = rootProject.file("gradle/toolchains.gradle"))

tasks.withType<CppCompile> {
    macros.put("LEVELDB_ATOMIC_PRESENT", "1")
    macros.put("SNAPPY", "1")
}

if (OperatingSystem.current().isLinux()) {
    library {
        source.from(
                project(":ldb:ldb-lib").file("src/leveldb/port/port_posix.cc"),
                project(":ldb:ldb-lib").file("src/leveldb/port/port_posix_sse.cc"),
                project(":ldb:ldb-lib").file("src/leveldb/port/env_posix.cc")
        )
    }

    tasks.withType<CppCompile> {
        macros.put("OS_LINUX", "1")
        macros.put("LEVELDB_PLATFORM_POSIX", "1")
        macros.put("_GLIBCXX_USE_CXX11_ABI", "0")

        compilerArgs.addAll(toolChain.map { toolChain: NativeToolChain ->
            val compilerSpecificArgs = arrayListOf<String>()
            if (toolChain is Gcc || toolChain is Clang) {
                compilerSpecificArgs += "-fPIC"
                compilerSpecificArgs += "-Wa,--mrelax-relocations=no"
                compilerSpecificArgs += "-std=c++11"
            }
            compilerSpecificArgs
        })
    }
}
