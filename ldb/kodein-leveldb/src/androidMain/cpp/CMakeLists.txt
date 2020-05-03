cmake_minimum_required(VERSION 3.4.1)

add_library( kodein-leveldb-jni SHARED
    ${PATH_BASE}/jni/src/main/cpp/leveldb-jni.cc
)

target_include_directories( kodein-leveldb-jni PUBLIC
    ${PATH_BASE}/jni/build/nativeHeaders
)

target_include_directories( kodein-leveldb-jni PRIVATE
    ${PATH_BASE}/lib/build/out/android-${ANDROID_ABI}/include
)

target_link_libraries( kodein-leveldb-jni
    ${PATH_BASE}/lib/build/out/android-${ANDROID_ABI}/lib/libfatleveldb.a
)
