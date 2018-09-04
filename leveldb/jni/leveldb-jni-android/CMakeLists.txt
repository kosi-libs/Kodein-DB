cmake_minimum_required(VERSION 3.4.1)

add_library(
    leveldb-jni-native

    SHARED

    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/snappy/snappy.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/snappy/snappy-stubs-internal.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/snappy/snappy-sinksource.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/util/options.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/util/logging.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/util/status.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/util/coding.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/util/bloom.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/util/crc32c.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/util/cache.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/util/comparator.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/util/arena.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/util/filter_policy.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/util/hash.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/util/env.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/util/histogram.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/db/version_set.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/db/version_edit.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/db/c.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/db/log_writer.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/db/dumpfile.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/db/builder.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/db/table_cache.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/db/write_batch.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/db/db_iter.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/db/dbformat.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/db/repair.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/db/log_reader.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/db/memtable.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/db/filename.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/db/db_impl.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/table/merger.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/table/filter_block.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/table/table.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/table/iterator.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/table/format.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/table/block.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/table/block_builder.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/table/two_level_iterator.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/table/table_builder.cc

    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/port/port_posix.cc
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/util/env_posix.cc

    ${CMAKE_CURRENT_SOURCE_DIR}/../leveldb-jni-native/src/main/cpp/leveldb-jni.cc
)

include_directories(
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/snappy
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb
    ${CMAKE_CURRENT_LIST_DIR}/../../lib/src/leveldb/include

    ${CMAKE_CURRENT_SOURCE_DIR}/../leveldb-jni-native/src/main/public
)

add_definitions(
    -DLEVELDB_ATOMIC_PRESENT
    -DSNAPPY
    -D_REENTRANT
    -DOS_ANDROID
    -DLEVELDB_PLATFORM_POSIX
)

set( CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wno-expansion-to-defined" )

find_library( log-lib log )

target_link_libraries( leveldb-jni-native ${log-lib} )
