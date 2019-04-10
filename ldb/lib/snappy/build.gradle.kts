
tasks.create<Delete>("clean") {
    delete(buildDir)
}

val srcDir = "${project(":ldb:lib").projectDir}/src/snappy"

val configure = tasks.create<Exec>("configure") {
    group = "build"

    workingDir("$buildDir/cmake")
    commandLine("cmake")
    args(
            "-DSNAPPY_BUILD_TESTS:BOOL=0",
            "-DCMAKE_C_COMPILER:STRING=clang",
            "-DCMAKE_CXX_COMPILER:STRING=clang++",
            "-DCMAKE_POSITION_INDEPENDENT_CODE:BOOL=1",
            "-DCMAKE_INSTALL_PREFIX:PATH=$buildDir/out",
            "-DCMAKE_C_FLAGS:STRING=-D_GLIBCXX_USE_CXX11_ABI=0",
            "-DCMAKE_CXX_FLAGS:STRING=-D_GLIBCXX_USE_CXX11_ABI=0",
            srcDir
    )

    inputs.dir(srcDir)
    outputs.dir(workingDir)

    doFirst {
        mkdir(workingDir)
    }
}

tasks.create<Exec>("build") {
    group = "build"

    dependsOn(configure)

    workingDir("$buildDir/cmake")
    commandLine("make")
    args(
            "all",
            "install",
            "VERBOSE=1"
    )

    inputs.dir(srcDir)
    inputs.dir(configure.workingDir)
    outputs.dir("${buildDir}/out")
}
