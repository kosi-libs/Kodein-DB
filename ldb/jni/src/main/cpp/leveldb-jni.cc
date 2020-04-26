#pragma ide diagnostic ignored "OCUnusedGlobalDeclarationInspection"


#include "kodein/org_kodein_db_leveldb_jni_Native.h"

#include "leveldb/cache.h"
#include "leveldb/db.h"
#include "leveldb/env.h"
#include "leveldb/filter_policy.h"
#include "leveldb/write_batch.h"

#include <chrono>
#include <iostream>


# if defined(__linux__) || defined(__CYGWIN__)
# 	include <endian.h>
# elif defined(__APPLE__)
#     include <libkern/OSByteOrder.h>
#     define htobe32(x) OSSwapHostToBigInt32(x)
# elif defined(__OpenBSD__) || defined(__NetBSD__) || defined(__FreeBSD__) || defined(__DragonFly__)
#     include <sys/endian.h>
# elif defined(_WIN16) || defined(_WIN32) || defined(_WIN64) || defined(__WINDOWS__)
#     include <windows.h>
#     if BYTE_ORDER == LITTLE_ENDIAN
#         if defined(_MSC_VER)
#             include <stdlib.h>
#             define htobe32(x) _byteswap_ulong(x)
#         elif defined(__GNUC__) || defined(__clang__)
#             define htobe32(x) __builtin_bswap32(x)
#         else
#             error platform not supported
#         endif
# 	 else
#         define htobe32(x) (x)
#     endif
# else
#     error platform not supported
# endif


static jclass LevelDBExceptionClass;

//class PrintLogger : public leveldb::Logger {
//    void Logv(const char* format, va_list ap) final;
//};
//
//void PrintLogger::Logv(const char* format, va_list ap) {
//    vprintf(format, ap);
//}

//extern "C" leveldb::Logger *soberLogger();

////////////////////////////////////////// UTILS //////////////////////////////////////////

void throwLevelDBExceptionFromMessage(JNIEnv *env, const std::string &message) {
	env->ThrowNew(LevelDBExceptionClass, message.c_str());
}

void throwLevelDBExceptionFromStatus(JNIEnv *env, const leveldb::Status &status) {
	throwLevelDBExceptionFromMessage(env, status.ToString());
}

char *getAsciiString(JNIEnv *env, jstring jstr) {
	int length = env->GetStringLength(jstr);
	const jchar *chars = env->GetStringCritical(jstr, nullptr);
	char *str = new char[length + 1];
	for (int i = 0; i < length; ++i)
		str[i] = (char) chars[i];
	str[length] = 0;
	env->ReleaseStringCritical(jstr, chars);
	return str;
}

class Bytes {
    JNIEnv *env;
    jbyteArray array;
    jbyte *addr;
    bool sync;

    static jbyte *getAddr(JNIEnv *env, jbyteArray array, bool sync) {
        if (sync)
            return env->GetByteArrayElements(array, nullptr);
        else
            return (jbyte *) env->GetPrimitiveArrayCritical(array, nullptr);
    }

public:
    leveldb::Slice slice;

    Bytes(JNIEnv *env, jobject buffer, jint offset, jint len, bool sync) : env(nullptr), array(nullptr), addr(nullptr), sync(sync), slice(leveldb::Slice(((char *) env->GetDirectBufferAddress(buffer)) + offset, len)) {}

    Bytes(JNIEnv *env, jbyteArray array, jbyte *addr, jint offset, jint len, bool sync) : env(env), array(array), addr(addr), sync(sync), slice(leveldb::Slice(((char *) addr) + offset, len)) {}

    Bytes(JNIEnv *env, jbyteArray array, jint offset, jint len, bool sync) : Bytes(env, array, getAddr(env, array, sync), offset, len, sync) {}

    ~Bytes() {
        if (env == nullptr || array == nullptr || addr == nullptr)
            return ;
        if (sync)
            env->ReleaseByteArrayElements(array, addr, JNI_ABORT);
        else
            env->ReleasePrimitiveArrayCritical(array, addr, JNI_ABORT);
    }

    Bytes(Bytes &&bytes) noexcept : env(bytes.env), array(bytes.array), addr(bytes.addr), sync(bytes.sync), slice(bytes.slice) {}
    Bytes(const Bytes &bytes) = delete;
};

#define _BYTES(b, s)    Bytes(env, b ## Bytes, b ## Offset, b ## Len, s)
#define BYTES_S(b)      _BYTES(b, true)
#define BYTES_A(b)      _BYTES(b, false)
#define BYTES(b)        _BYTES(b, sync)

#define CHECK_STATUS(F, R)    leveldb::Status status = F;                     \
                              if (!status.ok()) {                              \
                                  throwLevelDBExceptionFromStatus(env, status); \
                                  return R;                                      \
                              }

#define PTR(t, n)     reinterpret_cast<t*>(n ## Ptr)
#define CAST(t, n)    auto *n = PTR(t, n)

leveldb::WriteOptions _writeOptions(jboolean sync) {
	leveldb::WriteOptions options;
	if (sync)
		options.sync = true;
    return options;
}

leveldb::ReadOptions _readOptions(jboolean verifyChecksum, jboolean fillCache, jlong snapshotPtr) {
	leveldb::ReadOptions options;
	options.verify_checksums = verifyChecksum;
	options.fill_cache = fillCache;
	if (snapshotPtr != 0)
		options.snapshot = PTR(leveldb::Snapshot, snapshot);
    return options;
}

#define CHECK_IT_VALID(R)    if (!it->Valid()) {                                                \
                                 throwLevelDBExceptionFromMessage(env, "Cursor is not valid"); \
                                 return R;                                                        \
                             }

extern "C" {

////////////////////////////////////////// JNI (UN)LOAD //////////////////////////////////////////

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *) {
	JNIEnv *env;

	if (jvm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
		return JNI_ERR; /* JNI version not supported */
	}

	LevelDBExceptionClass = (jclass) env->NewGlobalRef(env->FindClass("org/kodein/db/leveldb/LevelDBException"));
	if (env->ExceptionCheck()) return JNI_ERR;

	return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *jvm, void *) {
	JNIEnv *env;

	if (jvm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
		return ;
	}

	env->DeleteGlobalRef(LevelDBExceptionClass);
}


////////////////////////////////////////// NATIVE BYTES //////////////////////////////////////////

JNIEXPORT jobject JNICALL Java_org_kodein_db_leveldb_jni_Native_bufferNew (JNIEnv *env, jclass, jlong valuePtr) {
    CAST(std::string, value);

    return env->NewDirectByteBuffer((void *) value->data(), value->size());
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_bufferRelease (JNIEnv *env, jclass, jlong valuePtr) {
    CAST(std::string, value);

	delete value;
}


////////////////////////////////////////////// OPTIONS //////////////////////////////////////////

JNIEXPORT jlong JNICALL Java_org_kodein_db_leveldb_jni_Native_optionsNew (JNIEnv *env, jclass,
    jboolean printLogs,
    jboolean createIfMissing,
    jboolean errorIfExists,
    jboolean paranoidChecks,
    jint writeBufferSize,
    jint maxOpenFiles,
    jint cacheSize,
    jint blockSize,
    jint blockRestartInterval,
    jint maxFileSize,
    jboolean snappyCompression,
    jboolean reuseLogs,
    jint bloomFilterBitsPerKey
    ) {
    auto *options = new leveldb::Options();

    // TODO
    options->info_log = nullptr; //printLogs ? new PrintLogger() : NULL;
    options->create_if_missing = createIfMissing;
    options->error_if_exists = errorIfExists;
    options->paranoid_checks = paranoidChecks;
    options->write_buffer_size = writeBufferSize;
    options->max_open_files = maxOpenFiles;
    options->block_cache = leveldb::NewLRUCache(cacheSize);
    options->block_size = blockSize;
    options->block_restart_interval = blockRestartInterval;
    options->max_file_size = maxFileSize;
    options->compression = snappyCompression ? leveldb::kSnappyCompression : leveldb::kNoCompression;
    options->reuse_logs = reuseLogs;
    options->filter_policy = (bloomFilterBitsPerKey <= 0) ? nullptr : leveldb::NewBloomFilterPolicy(bloomFilterBitsPerKey);

    return (jlong) options;
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_optionsRelease (JNIEnv *env, jclass, jlong optionsPtr) {
    CAST(leveldb::Options, options);

    delete options->info_log;

    delete options->block_cache;

    delete options->filter_policy;

    delete options;
}


////////////////////////////////////////////// LEVELDB //////////////////////////////////////////

JNIEXPORT jlong JNICALL Java_org_kodein_db_leveldb_jni_Native_dbOpen (JNIEnv *env, jclass, jstring jpath, jlong optionsPtr, jboolean repairOnCorruption) {
    CAST(leveldb::Options, options);

	const char *path = getAsciiString(env, jpath);

	leveldb::DB *ldb = nullptr;

	leveldb::Status status = leveldb::DB::Open(*options, path, &ldb);

    if (repairOnCorruption && status.IsCorruption()) {
        status = leveldb::RepairDB(path, *options);
        if (status.ok())
            status = leveldb::DB::Open(*options, path, &ldb);
    }

	delete[] path;

	if (!status.ok()) {
		throwLevelDBExceptionFromStatus(env, status);
		return 0;
	}

	return (jlong) ldb;
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_dbRelease (JNIEnv *, jclass, jlong ldbPtr) {
    CAST(leveldb::DB, ldb);

	delete ldb;
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_dbDestroy (JNIEnv *env, jclass, jstring jpath, jlong optionsPtr) {
    CAST(leveldb::Options, options);

	const char *path = getAsciiString(env, jpath);
    leveldb::Status s = leveldb::DestroyDB(path, *options);
    delete[] path;

	CHECK_STATUS(s,) // NOLINT(performance-unnecessary-copy-initialization)
}

void J_LevelDBJNI_Put (JNIEnv *env, jlong ldbPtr, Bytes &&key, Bytes &&value, jboolean sync) {
    CAST(leveldb::DB, ldb);

    CHECK_STATUS(ldb->Put(_writeOptions(sync), key.slice, value.slice),)
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_putBB (JNIEnv *env, jclass, jlong ldbPtr, jobject keyBytes, jint keyOffset, jint keyLen, jobject valueBytes, jint valueOffset, jint valueLen, jboolean sync) {
    J_LevelDBJNI_Put(env, ldbPtr, BYTES(key), BYTES(value), sync);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_putAB (JNIEnv *env, jclass, jlong ldbPtr, jbyteArray keyBytes, jint keyOffset, jint keyLen, jobject valueBytes, jint valueOffset, jint valueLen, jboolean sync) {
    // Order matters!
    auto value = BYTES(value);
    auto key = BYTES(key);
    J_LevelDBJNI_Put(env, ldbPtr, std::move(key), std::move(value), sync);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_putBA (JNIEnv *env, jclass, jlong ldbPtr, jobject keyBytes, jint keyOffset, jint keyLen, jbyteArray valueBytes, jint valueOffset, jint valueLen, jboolean sync) {
    // Order matters!
    auto key = BYTES(key);
    auto value = BYTES(value);
    J_LevelDBJNI_Put(env, ldbPtr, std::move(key), std::move(value), sync);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_putAA (JNIEnv *env, jclass, jlong ldbPtr, jbyteArray keyBytes, jint keyOffset, jint keyLen, jbyteArray valueBytes, jint valueOffset, jint valueLen, jboolean sync) {
    J_LevelDBJNI_Put(env, ldbPtr, BYTES(key), BYTES(value), sync);
}

void J_LevelDBJNI_Delete (JNIEnv *env, jlong ldbPtr, Bytes key, jboolean sync) {
    CAST(leveldb::DB, ldb);

    CHECK_STATUS(ldb->Delete(_writeOptions(sync), key.slice),)
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_deleteB (JNIEnv *env, jclass, jlong ldbPtr, jobject keyBytes, jint keyOffset, jint keyLen, jboolean sync) {
    J_LevelDBJNI_Delete(env, ldbPtr, BYTES(key), sync);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_deleteA (JNIEnv *env, jclass, jlong ldbPtr, jbyteArray keyBytes, jint keyOffset, jint keyLen, jboolean sync) {
    J_LevelDBJNI_Delete(env, ldbPtr, BYTES(key), sync);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_write (JNIEnv *env, jclass, jlong ldbPtr, jlong batchPtr, jboolean sync) {
    CAST(leveldb::DB, ldb);
    CAST(leveldb::WriteBatch, batch);

    CHECK_STATUS(ldb->Write(_writeOptions(sync), batch),)
}

jlong LevelDBJNI_Get (JNIEnv *env, leveldb::DB* ldb, const leveldb::Slice &key, const leveldb::ReadOptions &options) {
	auto *value = new std::string;
	leveldb::Status status = ldb->Get(options, key, value);

	if (!status.ok() && !status.IsNotFound()) {
		delete value;
		throwLevelDBExceptionFromStatus(env, status);
		return 0;
	}

	if (status.IsNotFound()) {
		delete value;
		return 0;
	}

    return (jlong) value;
}

jlong J_LevelDBJNI_Get(JNIEnv *env, jlong ldbPtr, Bytes key, jboolean verifyChecksum, jboolean fillCache, jlong snapshotPtr) {
    CAST(leveldb::DB, ldb);

    return LevelDBJNI_Get(env, ldb, key.slice, _readOptions(verifyChecksum, fillCache, snapshotPtr));
}

JNIEXPORT jlong JNICALL Java_org_kodein_db_leveldb_jni_Native_getB (JNIEnv *env, jclass, jlong ldbPtr, jobject keyBytes, jint keyOffset, jint keyLen, jboolean verifyChecksum, jboolean fillCache, jlong snapshotPtr) {
    return J_LevelDBJNI_Get(env, ldbPtr, BYTES_S(key), verifyChecksum, fillCache, snapshotPtr);
}

JNIEXPORT jlong JNICALL Java_org_kodein_db_leveldb_jni_Native_getA (JNIEnv *env, jclass, jlong ldbPtr, jbyteArray keyBytes, jint keyOffset, jint keyLen, jboolean verifyChecksum, jboolean fillCache, jlong snapshotPtr) {
    return J_LevelDBJNI_Get(env, ldbPtr, BYTES_S(key), verifyChecksum, fillCache, snapshotPtr);
}

jlong J_LevelDBJNI_IndirectGet (JNIEnv *env, jlong ldbPtr, Bytes key, jboolean verifyChecksum, jboolean fillCache, jlong snapshotPtr) {
    CAST(leveldb::DB, ldb);

    leveldb::ReadOptions options = _readOptions(verifyChecksum, fillCache, snapshotPtr);

	std::string indir;
	leveldb::Status status = ldb->Get(options, key.slice, &indir);

	if (!status.ok() && !status.IsNotFound()) {
		throwLevelDBExceptionFromStatus(env, status);
		return 0;
	}

	if (status.IsNotFound())
		return 0;

    return LevelDBJNI_Get(env, ldb, leveldb::Slice(indir), options);
}

JNIEXPORT jlong JNICALL Java_org_kodein_db_leveldb_jni_Native_indirectGetB (JNIEnv *env, jclass, jlong ldbPtr, jobject keyBytes, jint keyOffset, jint keyLen, jboolean verifyChecksum, jboolean fillCache, jlong snapshotPtr) {
    return J_LevelDBJNI_IndirectGet(env, ldbPtr, BYTES_S(key), verifyChecksum, fillCache, snapshotPtr);
}

JNIEXPORT jlong JNICALL Java_org_kodein_db_leveldb_jni_Native_indirectGetA (JNIEnv *env, jclass, jlong ldbPtr, jbyteArray keyBytes, jint keyOffset, jint keyLen, jboolean verifyChecksum, jboolean fillCache, jlong snapshotPtr) {
    return J_LevelDBJNI_IndirectGet(env, ldbPtr, BYTES_S(key), verifyChecksum, fillCache, snapshotPtr);
}

JNIEXPORT jlong JNICALL Java_org_kodein_db_leveldb_jni_Native_indirectGetI (JNIEnv *env, jclass, jlong ldbPtr, jlong itPtr, jboolean verifyChecksum, jboolean fillCache, jlong snapshotPtr) {
    CAST(leveldb::DB, ldb);
    CAST(leveldb::Iterator, it);

	CHECK_IT_VALID(0)

    return LevelDBJNI_Get(env, ldb, it->value(), _readOptions(verifyChecksum, fillCache, snapshotPtr));
}


////////////////////////////////////////// ITERATOR //////////////////////////////////////////

JNIEXPORT jlong JNICALL Java_org_kodein_db_leveldb_jni_Native_iteratorNew (JNIEnv *, jclass, jlong ldbPtr, jboolean verifyChecksum, jboolean fillCache, jlong snapshotPtr) {
    CAST(leveldb::DB, ldb);

	return (jlong) ldb->NewIterator(_readOptions(verifyChecksum, fillCache, snapshotPtr));
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_iteratorRelease (JNIEnv *env, jclass, jlong itPtr) {
    CAST(leveldb::Iterator, it);

	delete it;
}

//JNIEXPORT jboolean JNICALL Java_org_kodein_db_leveldb_jni_Native_iteratorValid (JNIEnv *env, jclass, jlong itPtr) {
//    CAST(leveldb::Iterator, it);
//
//	return it->Valid();
//}

void putLens(JNIEnv *env, leveldb::Iterator *it, jintArray lens) {
    bool valid = it->Valid();

    auto ints = (jint *) env->GetPrimitiveArrayCritical(lens, nullptr);
    ints[0] = valid ? (int) it->key().size() : -1;
    ints[1] = valid ? (int) it->value().size() : -1;
    env->ReleasePrimitiveArrayCritical(lens, ints, 0);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_iteratorSeekToFirst (JNIEnv *env, jclass, jlong itPtr, jintArray lens) {
    CAST(leveldb::Iterator, it);

	it->SeekToFirst();

    CHECK_STATUS(it->status(), )

    putLens(env, it, lens);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_iteratorSeekToLast (JNIEnv *env, jclass, jlong itPtr, jintArray lens) {
    CAST(leveldb::Iterator, it);

	it->SeekToLast();

    CHECK_STATUS(it->status(), )

    putLens(env, it, lens);
}

void J_LevelDBJNI_Iterator_Seek(JNIEnv *env, jlong itPtr, Bytes key, jintArray lens) {
    CAST(leveldb::Iterator, it);

    it->Seek(key.slice);

    CHECK_STATUS(it->status(), )

    putLens(env, it, lens);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_iteratorSeekB (JNIEnv *env, jclass, jlong itPtr, jobject keyBytes, jint keyOffset, jint keyLen, jintArray lens) {
    J_LevelDBJNI_Iterator_Seek(env, itPtr, BYTES_S(key), lens);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_iteratorSeekA (JNIEnv *env, jclass, jlong itPtr, jbyteArray keyBytes, jint keyOffset, jint keyLen, jintArray lens) {
    J_LevelDBJNI_Iterator_Seek(env, itPtr, BYTES_S(key), lens);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_iteratorNext (JNIEnv *env, jclass, jlong itPtr, jintArray lens) {
    CAST(leveldb::Iterator, it);

	it->Next();

    CHECK_STATUS(it->status(), )

    putLens(env, it, lens);
}

JNIEXPORT void Java_org_kodein_db_leveldb_jni_Native_iteratorPrev (JNIEnv *env, jclass, jlong itPtr, jintArray lens) {
    CAST(leveldb::Iterator, it);

	it->Prev();

    CHECK_STATUS(it->status(), )

    putLens(env, it, lens);
}

//jobject toBuffer(JNIEnv *env, leveldb::Slice slice, jobject buffer) {
//    if (buffer == nullptr || (slice.size() + 4) > env->GetDirectBufferCapacity(buffer)) {
//        if (buffer != nullptr) {
//            delete[] (char *) env->GetDirectBufferAddress(buffer);
//        }
//        size_t cap = (((slice.size() + 4) / 1024) + 1) * 1024;
//        buffer = env->NewDirectByteBuffer(new char[cap], cap);
//    }
//
//    char *buf = (char *) env->GetDirectBufferAddress(buffer);
//    *((int32_t *) buf) = htobe32(slice.size());
//    memcpy((buf + 4), slice.data(), slice.size());
//
//    return buffer;
//}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_iteratorKey (JNIEnv *env, jclass, jlong itPtr, jobject buffer) {
    CAST(leveldb::Iterator, it);

//	CHECK_IT_VALID(nullptr)

	leveldb::Slice key = it->key();

    memcpy(env->GetDirectBufferAddress(buffer), key.data(), key.size());

//	return toBuffer(env, key, buffer);
//	return env->NewDirectByteBuffer((void *) key.data(), key.size());
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_iteratorValue (JNIEnv *env, jclass, jlong itPtr, jobject buffer) {
    CAST(leveldb::Iterator, it);

//	CHECK_IT_VALID(nullptr)

	leveldb::Slice value = it->value();

    memcpy(env->GetDirectBufferAddress(buffer), value.data(), value.size());

//  return toBuffer(env, value, buffer);
//	return env->NewDirectByteBuffer((void *) value.data(), value.size());
}


////////////////////////////////////////// SNAPSHOT //////////////////////////////////////////

JNIEXPORT jlong JNICALL Java_org_kodein_db_leveldb_jni_Native_snapshotNew (JNIEnv *, jclass, jlong ldbPtr) {
    CAST(leveldb::DB, ldb);

	return (jlong) ldb->GetSnapshot();
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_snapshotRelease (JNIEnv *env, jclass, jlong ldbPtr, jlong snapshotPtr) {
    CAST(leveldb::DB, ldb);
    CAST(leveldb::Snapshot, snapshot);

	ldb->ReleaseSnapshot(snapshot);
}


////////////////////////////////////////// WRITE BATCH //////////////////////////////////////////

JNIEXPORT jlong JNICALL Java_org_kodein_db_leveldb_jni_Native_writeBatchNew (JNIEnv *, jclass) {
	return (jlong) new leveldb::WriteBatch;
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_writeBatchRelease (JNIEnv *env, jclass, jlong batchPtr) {
    CAST(leveldb::WriteBatch, batch);

	delete batch;
}

void J_LevelDBJNI_WriteBatch_Put (jlong batchPtr, Bytes &&key, Bytes &&value) {
    CAST(leveldb::WriteBatch, batch);
	batch->Put(key.slice, value.slice);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_writeBatchPutBB (JNIEnv *env, jclass, jlong batchPtr, jobject keyBytes, jint keyOffset, jint keyLen, jobject valueBytes, jint valueOffset, jint valueLen) {
    J_LevelDBJNI_WriteBatch_Put(batchPtr, BYTES_A(key), BYTES_A(value));
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_writeBatchPutAB (JNIEnv *env, jclass, jlong batchPtr, jbyteArray keyBytes, jint keyOffset, jint keyLen, jobject valueBytes, jint valueOffset, jint valueLen) {
    // Order matters!
    auto value = BYTES_A(value);
    auto key = BYTES_A(key);
    J_LevelDBJNI_WriteBatch_Put(batchPtr, std::move(key), std::move(value));
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_writeBatchPutBA (JNIEnv *env, jclass, jlong batchPtr, jobject keyBytes, jint keyOffset, jint keyLen, jbyteArray valueBytes, jint valueOffset, jint valueLen) {
    // Order matters!
    auto key = BYTES_A(key);
    auto value = BYTES_A(value);
    J_LevelDBJNI_WriteBatch_Put(batchPtr, std::move(key), std::move(value));
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_writeBatchPutAA (JNIEnv *env, jclass, jlong batchPtr, jbyteArray keyBytes, jint keyOffset, jint keyLen, jbyteArray valueBytes, jint valueOffset, jint valueLen) {
    J_LevelDBJNI_WriteBatch_Put(batchPtr, BYTES_A(key), BYTES_A(value));
}

void J_LevelDBJNI_WriteBatch_Delete(jlong batchPtr, Bytes key) {
    CAST(leveldb::WriteBatch, batch);

	batch->Delete(key.slice);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_writeBatchDeleteB (JNIEnv *env, jclass, jlong batchPtr, jobject keyBytes, jint keyOffset, jint keyLen) {
    J_LevelDBJNI_WriteBatch_Delete(batchPtr, BYTES_A(key));
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_writeBatchDeleteA (JNIEnv *env, jclass, jlong batchPtr, jbyteArray keyBytes, jint keyOffset, jint keyLen) {
    J_LevelDBJNI_WriteBatch_Delete(batchPtr, BYTES_A(key));
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_writeBatchClear (JNIEnv *env, jclass, jlong batchPtr) {
    CAST(leveldb::WriteBatch, batch);

    batch->Clear();
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_Native_writeBatchAppend (JNIEnv *env, jclass, jlong batchPtr, jlong sourcePtr) {
    CAST(leveldb::WriteBatch, batch);
    CAST(leveldb::WriteBatch, source);

    batch->Append(*source);
}


} /* extern "C" */
