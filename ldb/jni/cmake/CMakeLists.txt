cmake_minimum_required(VERSION 3.13)
project(LevelDB-JNI)

set(CMAKE_CXX_STANDARD 14)

add_library(
        kodein-leveldb-jni
        ../src/main/cpp/leveldb-jni.cc
)

include_directories(
        kodein-leveldb-jni
        ../build/nativeHeaders
        ../../lib/src/leveldb/include
        /usr/lib/jvm/java-11-openjdk-amd64/include
        /usr/lib/jvm/java-11-openjdk-amd64/include/linux
)