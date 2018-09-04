import org.gradle.internal.os.OperatingSystem

plugins {
    `cpp-library`
}

library {
    linkage.set(listOf(Linkage.STATIC))

    source.from(
            fileTree("$rootDir/leveldb/lib/src/leveldb/db") { exclude("*_test.cc", "leveldb_main.cc") },
            fileTree("$rootDir/leveldb/lib/src/leveldb/table") { exclude("*_test.cc") },
            fileTree("$rootDir/leveldb/lib/src/leveldb/util") { exclude("*_test.cc") },
            fileTree("$rootDir/leveldb/lib/src/snappy")
    )

    privateHeaders.from(
            "$rootDir/leveldb/lib/src/snappy",
            "$rootDir/leveldb/lib/src/leveldb"
    )

    publicHeaders.from("$rootDir/leveldb/lib/src/leveldb/include")
}

tasks.withType<CppCompile> {
    macros.put("LEVELDB_ATOMIC_PRESENT", "1")
    macros.put("SNAPPY", "1")
}

if (OperatingSystem.current().isLinux()) {
    library {
        source.from(
                "$rootDir/leveldb/lib/src/leveldb/port/port_posix.cc",
                "$rootDir/leveldb/lib/src/leveldb/port/env_posix.cc"
        )
        privateHeaders.from(
                "$rootDir/leveldb/lib/src/dlmalloc"
        )

        binaries.configureEach {
            if (toolChain is Gcc || toolChain is Clang) {
                (toolChain as Gcc)
            }
        }
    }

    tasks.withType<CppCompile> {
        macros.put("OS_LINUX", "1")
        macros.put("LEVELDB_PLATFORM_POSIX", "1")

        compilerArgs.addAll(toolChain.map { toolChain: NativeToolChain ->
            val compilerSpecificArgs = arrayListOf<String>()
            if (toolChain is Gcc || toolChain is Clang) {
                compilerSpecificArgs += "-fPIC"
            }
            compilerSpecificArgs
        })
    }
}
