#include <stdint.h>

#include "kodein/org_kodein_db_leveldb_jni_LevelDBNative.h"
#include "kodein/org_kodein_db_leveldb_jni_LevelDBNative_NativeBytes.h"
#include "kodein/org_kodein_db_leveldb_jni_LevelDBNative_WriteBatch.h"
#include "kodein/org_kodein_db_leveldb_jni_LevelDBNative_Snapshot.h"
#include "kodein/org_kodein_db_leveldb_jni_LevelDBNative_Iterator.h"
#include "kodein/org_kodein_db_leveldb_jni_LevelDBNative_Iterator_NativeBytesArray.h"

#include "leveldb/cache.h"
#include "leveldb/db.h"
#include "leveldb/env.h"
#include "leveldb/filter_policy.h"
#include "leveldb/write_batch.h"

#include <sstream>
#include <ctime>
#include <iostream>


static jclass LevelDBExceptionClass;

//extern "C" leveldb::Logger *soberLogger();

////////////////////////////////////////// UTILS //////////////////////////////////////////

void throwLevelDBExceptionFromMessage(JNIEnv *env, const std::string &message) {
	env->ThrowNew(LevelDBExceptionClass, message.c_str());
}

void throwLevelDBExceptionFromStatus(JNIEnv *env, leveldb::Status status) {
	throwLevelDBExceptionFromMessage(env, status.ToString());
}

char *getAsciiString(JNIEnv *env, jstring jstr) {
	int length = env->GetStringLength(jstr);
	const jchar *chars = env->GetStringCritical(jstr, 0);
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
            return env->GetByteArrayElements(array, 0);
        else
            return (jbyte *) env->GetPrimitiveArrayCritical(array, 0);
    }

public:
    leveldb::Slice slice;

    Bytes(JNIEnv *env, jobject buffer, jint offset, jint len, bool sync) : env(0), array(0), addr(0), sync(sync), slice(leveldb::Slice(((char *) env->GetDirectBufferAddress(buffer)) + offset, len)) {}

    Bytes(JNIEnv *env, jbyteArray array, jbyte *addr, jint offset, jint len, bool sync) : env(env), array(array), addr(addr), sync(sync), slice(leveldb::Slice(((char *) addr) + offset, len)) {}

    Bytes(JNIEnv *env, jbyteArray array, jint offset, jint len, bool sync) : Bytes(env, array, getAddr(env, array, sync), offset, len, sync) {}

    ~Bytes() {
        if (env == 0 || array == 0 || addr == 0)
            return ;
        if (sync)
            env->ReleaseByteArrayElements(array, addr, JNI_ABORT);
        else
            env->ReleasePrimitiveArrayCritical(array, addr, JNI_ABORT);
    }

    Bytes(Bytes &&bytes) : env(bytes.env), array(bytes.array), addr(bytes.addr), sync(bytes.sync), slice(std::move(bytes.slice)) {}

private:
    Bytes(const Bytes &bytes) {}
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
#define CAST(t, n)    t *n = PTR(t, n)

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
                                 throwLevelDBExceptionFromMessage(env, "Iterator is not valid"); \
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

class NoCache : public leveldb::Cache {
    uint64_t _id;
public:
    NoCache() : _id(0) {}
    Handle *Insert(const leveldb::Slice&, void*, size_t, void (*)(const leveldb::Slice&, void*)) { return NULL; }
    Handle *Lookup(const leveldb::Slice&) { return NULL; }
    void Release(Handle*) {}
    void *Value(Handle*) { return NULL; }
    void Erase(const leveldb::Slice&) {}
    uint64_t NewId() { return _id++; }
};


////////////////////////////////////////////// LEVELDB //////////////////////////////////////////

JNIEXPORT jlong JNICALL JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_n_1NewOptions (JNIEnv *env, jclass,
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
    jint bloomFilterBitsPerKey
    ) {
    leveldb::Options *options = new leveldb::Options();

    // TODO
    options->info_log = NULL;
//    options->info_log = printLogs ? soberLogger() : NULL;
    options->create_if_missing = createIfMissing;
    options->error_if_exists = errorIfExists;
    options->paranoid_checks = paranoidChecks;
    options->write_buffer_size = writeBufferSize;
    options->max_open_files = maxOpenFiles;
    options->block_cache = (cacheSize <= 0) ? new NoCache() : leveldb::NewLRUCache(cacheSize);
    options->block_size = blockSize;
    options->block_restart_interval = blockRestartInterval;
    // TODO Update to LevelDB 1.10
//    options->max_file_size = maxFileSize;
    options->compression = snappyCompression ? leveldb::kSnappyCompression : leveldb::kNoCompression;
    options->filter_policy = (bloomFilterBitsPerKey <= 0) ? NULL : leveldb::NewBloomFilterPolicy(bloomFilterBitsPerKey);

    return (jlong) options;
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_n_1ReleaseOptions (JNIEnv *env, jclass, jlong optionsPtr) {
    CAST(leveldb::Options, options);

    if (options->info_log != NULL)
        delete options->info_log;

    if (options->block_cache != NULL)
        delete options->block_cache;

    if (options->filter_policy != NULL)
        delete options->filter_policy;

    delete options;
}

JNIEXPORT jlong JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_n_1OpenDB (JNIEnv *env, jclass, jstring jpath, jlong optionsPtr, jboolean repairOnCorruption) {
    CAST(leveldb::Options, options);

	const char *path = getAsciiString(env, jpath);

	leveldb::DB *ldb = 0;

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

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_n_1DestroyDB (JNIEnv *env, jclass, jstring jpath) {
	const char *path = getAsciiString(env, jpath);
    leveldb::Status s = leveldb::DestroyDB(path, leveldb::Options());
    delete[] path;

	CHECK_STATUS(s,);
}

void J_LevelDBNative_Put (JNIEnv *env, jlong ldbPtr, Bytes key, Bytes value, jboolean sync) {
    CAST(leveldb::DB, ldb);

    CHECK_STATUS(ldb->Put(_writeOptions(sync), key.slice, value.slice),);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_n_1Put_1BB (JNIEnv *env, jclass, jlong ldbPtr, jobject keyBytes, jint keyOffset, jint keyLen, jobject valueBytes, jint valueOffset, jint valueLen, jboolean sync) {
    J_LevelDBNative_Put(env, ldbPtr, BYTES(key), BYTES(value), sync);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_n_1Put_1AB (JNIEnv *env, jclass, jlong ldbPtr, jbyteArray keyBytes, jint keyOffset, jint keyLen, jobject valueBytes, jint valueOffset, jint valueLen, jboolean sync) {
    J_LevelDBNative_Put(env, ldbPtr, BYTES(key), BYTES(value), sync);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_n_1Put_1BA (JNIEnv *env, jclass, jlong ldbPtr, jobject keyBytes, jint keyOffset, jint keyLen, jbyteArray valueBytes, jint valueOffset, jint valueLen, jboolean sync) {
    J_LevelDBNative_Put(env, ldbPtr, BYTES(key), BYTES(value), sync);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_n_1Put_1AA (JNIEnv *env, jclass, jlong ldbPtr, jbyteArray keyBytes, jint keyOffset, jint keyLen, jbyteArray valueBytes, jint valueOffset, jint valueLen, jboolean sync) {
    J_LevelDBNative_Put(env, ldbPtr, BYTES(key), BYTES(value), sync);
}

void J_LevelDBNative_Delete (JNIEnv *env, jlong ldbPtr, Bytes key, jboolean sync) {
    CAST(leveldb::DB, ldb);

    CHECK_STATUS(ldb->Delete(_writeOptions(sync), key.slice),);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_n_1Delete_1B (JNIEnv *env, jclass, jlong ldbPtr, jobject keyBytes, jint keyOffset, jint keyLen, jboolean sync) {
    J_LevelDBNative_Delete(env, ldbPtr, BYTES(key), sync);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_n_1Delete_1A (JNIEnv *env, jclass, jlong ldbPtr, jbyteArray keyBytes, jint keyOffset, jint keyLen, jboolean sync) {
    J_LevelDBNative_Delete(env, ldbPtr, BYTES(key), sync);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_n_1Write (JNIEnv *env, jclass, jlong ldbPtr, jlong batchPtr, jboolean sync) {
    CAST(leveldb::DB, ldb);
    CAST(leveldb::WriteBatch, batch);

    CHECK_STATUS(ldb->Write(_writeOptions(sync), batch),);
}

jlong LevelDBNative_Get (JNIEnv *env, leveldb::DB* ldb, const leveldb::Slice &key, const leveldb::ReadOptions &options) {
	std::string *value = new std::string;
	leveldb::Status status = ldb->Get(options, key, value);

	if (!status.ok() && !status.IsNotFound()) {
		delete value;
		throwLevelDBExceptionFromStatus(env, status);
		return 0;
	}

	if (value->empty() || status.IsNotFound()) {
		delete value;
		return 0;
	}

    return (jlong) value;
}

jlong J_LevelDBNative_Get(JNIEnv *env, jlong ldbPtr, Bytes key, jboolean verifyChecksum, jboolean fillCache, jlong snapshotPtr) {
    CAST(leveldb::DB, ldb);

    return LevelDBNative_Get(env, ldb, key.slice, _readOptions(verifyChecksum, fillCache, snapshotPtr));
}

JNIEXPORT jlong JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_n_1Get_1B (JNIEnv *env, jclass, jlong ldbPtr, jobject keyBytes, jint keyOffset, jint keyLen, jboolean verifyChecksum, jboolean fillCache, jlong snapshotPtr) {
    return J_LevelDBNative_Get(env, ldbPtr, BYTES_S(key), verifyChecksum, fillCache, snapshotPtr);
}

JNIEXPORT jlong JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_n_1Get_1A (JNIEnv *env, jclass, jlong ldbPtr, jbyteArray keyBytes, jint keyOffset, jint keyLen, jboolean verifyChecksum, jboolean fillCache, jlong snapshotPtr) {
    return J_LevelDBNative_Get(env, ldbPtr, BYTES_S(key), verifyChecksum, fillCache, snapshotPtr);
}

jlong J_LevelDBNative_IndirectGet (JNIEnv *env, jlong ldbPtr, Bytes key, jboolean verifyChecksum, jboolean fillCache, jlong snapshotPtr) {
    CAST(leveldb::DB, ldb);

    leveldb::ReadOptions options = _readOptions(verifyChecksum, fillCache, snapshotPtr);

	std::string indir;
	leveldb::Status status = ldb->Get(options, key.slice, &indir);

	if (!status.ok() && !status.IsNotFound()) {
		throwLevelDBExceptionFromStatus(env, status);
		return 0;
	}

	if (indir.empty() || status.IsNotFound())
		return 0;

    return LevelDBNative_Get(env, ldb, leveldb::Slice(indir), options);
}

JNIEXPORT jlong JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_n_1IndirectGet_1B (JNIEnv *env, jclass, jlong ldbPtr, jobject keyBytes, jint keyOffset, jint keyLen, jboolean verifyChecksum, jboolean fillCache, jlong snapshotPtr) {
    return J_LevelDBNative_IndirectGet(env, ldbPtr, BYTES_S(key), verifyChecksum, fillCache, snapshotPtr);
}

JNIEXPORT jlong JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_n_1IndirectGet_1A (JNIEnv *env, jclass, jlong ldbPtr, jbyteArray keyBytes, jint keyOffset, jint keyLen, jboolean verifyChecksum, jboolean fillCache, jlong snapshotPtr) {
    return J_LevelDBNative_IndirectGet(env, ldbPtr, BYTES_S(key), verifyChecksum, fillCache, snapshotPtr);
}

JNIEXPORT jlong JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_n_1IndirectGet_1I (JNIEnv *env, jclass, jlong ldbPtr, jlong itPtr, jboolean verifyChecksum, jboolean fillCache, jlong snapshotPtr) {
    CAST(leveldb::DB, ldb);
    CAST(leveldb::Iterator, it);

	CHECK_IT_VALID(0);

    return LevelDBNative_Get(env, ldb, it->value(), _readOptions(verifyChecksum, fillCache, snapshotPtr));
}

JNIEXPORT jlong JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_n_1NewIterator (JNIEnv *, jclass, jlong ldbPtr, jboolean verifyChecksum, jboolean fillCache, jlong snapshotPtr) {
    CAST(leveldb::DB, ldb);

	return (jlong) ldb->NewIterator(_readOptions(verifyChecksum, fillCache, snapshotPtr));
}

JNIEXPORT jlong JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_n_1NewSnapshot (JNIEnv *, jclass, jlong ldbPtr) {
    CAST(leveldb::DB, ldb);

	return (jlong) ldb->GetSnapshot();
}

JNIEXPORT jlong JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_n_1NewWriteBatch (JNIEnv *, jclass) {
	return (jlong) new leveldb::WriteBatch;
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_n_1Release (JNIEnv *, jclass, jlong ldbPtr) {
    CAST(leveldb::DB, ldb);

	delete ldb;
}


////////////////////////////////////////// NATIVE BYTES //////////////////////////////////////////

JNIEXPORT jobject JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_00024NativeBytes_n_1Buffer (JNIEnv *env, jclass, jlong valuePtr) {
    CAST(std::string, value);
    return env->NewDirectByteBuffer((void *) value->data(), value->size());
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_00024NativeBytes_n_1Release (JNIEnv *env, jclass, jlong valuePtr) {
    CAST(std::string, value);

	delete value;
}


////////////////////////////////////////// WRITE BATCH //////////////////////////////////////////

void J_LevelDBNative_WriteBatch_Put (jlong batchPtr, Bytes key, Bytes value) {
    CAST(leveldb::WriteBatch, batch);
	batch->Put(key.slice, value.slice);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_00024WriteBatch_n_1Put_1BB (JNIEnv *env, jclass, jlong batchPtr, jobject keyBytes, jint keyOffset, jint keyLen, jobject valueBytes, jint valueOffset, jint valueLen) {
    J_LevelDBNative_WriteBatch_Put(batchPtr, BYTES_A(key), BYTES_A(value));
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_00024WriteBatch_n_1Put_1AB (JNIEnv *env, jclass, jlong batchPtr, jbyteArray keyBytes, jint keyOffset, jint keyLen, jobject valueBytes, jint valueOffset, jint valueLen) {
    J_LevelDBNative_WriteBatch_Put(batchPtr, BYTES_A(key), BYTES_A(value));
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_00024WriteBatch_n_1Put_1BA (JNIEnv *env, jclass, jlong batchPtr, jobject keyBytes, jint keyOffset, jint keyLen, jbyteArray valueBytes, jint valueOffset, jint valueLen) {
    J_LevelDBNative_WriteBatch_Put(batchPtr, BYTES_A(key), BYTES_A(value));
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_00024WriteBatch_n_1Put_1AA (JNIEnv *env, jclass, jlong batchPtr, jbyteArray keyBytes, jint keyOffset, jint keyLen, jbyteArray valueBytes, jint valueOffset, jint valueLen) {
    J_LevelDBNative_WriteBatch_Put(batchPtr, BYTES_A(key), BYTES_A(value));
}

void J_LevelDBNative_WriteBatch_Delete(jlong batchPtr, Bytes key) {
    CAST(leveldb::WriteBatch, batch);

	batch->Delete(key.slice);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_00024WriteBatch_n_1Delete_1B (JNIEnv *env, jclass, jlong batchPtr, jobject keyBytes, jint keyOffset, jint keyLen) {
    J_LevelDBNative_WriteBatch_Delete(batchPtr, BYTES_A(key));
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_00024WriteBatch_n_1Delete_1A (JNIEnv *env, jclass, jlong batchPtr, jbyteArray keyBytes, jint keyOffset, jint keyLen) {
    J_LevelDBNative_WriteBatch_Delete(batchPtr, BYTES_A(key));
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_00024WriteBatch_n_1Release (JNIEnv *env, jclass, jlong batchPtr) {
    CAST(leveldb::WriteBatch, batch);

	delete batch;
}


////////////////////////////////////////// SNAPSHOT //////////////////////////////////////////

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_00024Snapshot_n_1Release (JNIEnv *env, jclass, jlong ldbPtr, jlong snapshotPtr) {
    CAST(leveldb::DB, ldb);
    CAST(leveldb::Snapshot, snapshot);

	ldb->ReleaseSnapshot(snapshot);
}


////////////////////////////////////////// ITERATOR //////////////////////////////////////////

JNIEXPORT jboolean JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_00024Iterator_n_1Valid (JNIEnv *env, jclass, jlong itPtr) {
    CAST(leveldb::Iterator, it);

	return it->Valid();
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_00024Iterator_n_1SeekToFirst (JNIEnv *env, jclass, jlong itPtr) {
    CAST(leveldb::Iterator, it);

	it->SeekToFirst();

    CHECK_STATUS(it->status(),);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_00024Iterator_n_1SeekToLast (JNIEnv *env, jclass, jlong itPtr) {
    CAST(leveldb::Iterator, it);

	it->SeekToLast();

    CHECK_STATUS(it->status(),);
}

void J_LevelDBNative_Iterator_Seek(JNIEnv *env, jlong itPtr, Bytes key) {
    CAST(leveldb::Iterator, it);

    it->Seek(key.slice);

    CHECK_STATUS(it->status(),);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_00024Iterator_n_1Seek_1B (JNIEnv *env, jclass, jlong itPtr, jobject keyBytes, jint keyOffset, jint keyLen) {
    J_LevelDBNative_Iterator_Seek(env, itPtr, BYTES_S(key));
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_00024Iterator_n_1Seek_1A (JNIEnv *env, jclass, jlong itPtr, jbyteArray keyBytes, jint keyOffset, jint keyLen) {
    J_LevelDBNative_Iterator_Seek(env, itPtr, BYTES_S(key));
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_00024Iterator_n_1Next (JNIEnv *env, jclass, jlong itPtr) {
    CAST(leveldb::Iterator, it);

	CHECK_IT_VALID();

	it->Next();

    CHECK_STATUS(it->status(),);
}

JNIEXPORT void Java_org_kodein_db_leveldb_jni_LevelDBNative_00024Iterator_n_1Prev (JNIEnv *env, jclass, jlong itPtr) {
    CAST(leveldb::Iterator, it);

	CHECK_IT_VALID();

	it->Prev();

    CHECK_STATUS(it->status(),);
}

JNIEXPORT jobject JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_00024Iterator_n_1key (JNIEnv *env, jclass, jlong itPtr) {
    CAST(leveldb::Iterator, it);

	CHECK_IT_VALID(0);

	leveldb::Slice key = it->key();

    CHECK_STATUS(it->status(), 0);

	return env->NewDirectByteBuffer((void *) key.data(), key.size());
}

JNIEXPORT jobject JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_00024Iterator_n_1value(JNIEnv *env, jclass, jlong itPtr) {
    CAST(leveldb::Iterator, it);

	CHECK_IT_VALID(0);

	leveldb::Slice value = it->value();

    CHECK_STATUS(it->status(), 0);

	return env->NewDirectByteBuffer((void *) value.data(), value.size());
}

JNIEXPORT void JNICALL JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_00024Iterator_n_1NextArray (JNIEnv *env, jclass, jlong itPtr, jlongArray ptrArray, jobjectArray buffers, jintArray indexArray, jintArray keyArray, jintArray valueArray, jintArray limitArray, jint bufferSize) {
    CAST(leveldb::Iterator, it);

    jlong *ptrs = env->GetLongArrayElements(ptrArray, NULL);
    jint *indexes = env->GetIntArrayElements(indexArray, NULL);
    jint *keys = env->GetIntArrayElements(keyArray, NULL);
    jint *values = env->GetIntArrayElements(valueArray, NULL);
    jint *limits = env->GetIntArrayElements(limitArray, NULL);

    int length = env->GetArrayLength(indexArray);

    int pos = 0;
    int index = -1;
    char *ptr = 0;

    for (int i = 0; i < length; ++i) {
        if (!it->Valid()) {
            indexes[i] = -1;
            break ;
        }

        leveldb::Slice key = it->key();
        leveldb::Slice value = it->value();

        if (ptr == 0 || (pos + key.size() + value.size()) > bufferSize) {
            int realBufferSize = std::max((int) (key.size() + value.size()), bufferSize);
            ptr = new char[realBufferSize];
            jobject buffer = env->NewDirectByteBuffer((void *) ptr, realBufferSize);
            ++index;
            ptrs[index] = (jlong) ptr;
            env->SetObjectArrayElement(buffers, index, buffer);

            pos = 0;
        }

        indexes[i] = index;

        memcpy(ptr + pos, key.data(), key.size());
        keys[i] = pos;
        pos += key.size();
        memcpy(ptr + pos, value.data(), value.size());
        values[i] = pos;
        pos += value.size();
        limits[i] = pos;

        it->Next();
    }

    env->ReleaseIntArrayElements(limitArray, limits, JNI_COMMIT);
    env->ReleaseIntArrayElements(valueArray, values, JNI_COMMIT);
    env->ReleaseIntArrayElements(keyArray, keys, JNI_COMMIT);
    env->ReleaseIntArrayElements(indexArray, indexes, JNI_COMMIT);
    env->ReleaseLongArrayElements(ptrArray, ptrs, JNI_COMMIT);
}

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_00024Iterator_n_1Release (JNIEnv *env, jclass, jlong itPtr) {
    CAST(leveldb::Iterator, it);

	delete it;
}

////////////////////////////////////////// ITERATOR::NATIVEBYTESARRAY //////////////////////////////////////////

JNIEXPORT void JNICALL Java_org_kodein_db_leveldb_jni_LevelDBNative_00024Iterator_00024NativeBytesArray_n_1Release (JNIEnv *env , jclass, jlongArray ptrArray) {
    jlong *ptrs = env->GetLongArrayElements(ptrArray, NULL);
    int length = env->GetArrayLength(ptrArray);

    for (int i = 0; i < length; ++i) {
        if (ptrs[i] == 0)
            break ;
        delete (char*) ptrs[i];
    }

    env->ReleaseLongArrayElements(ptrArray, ptrs, JNI_ABORT);
}


} /* extern "C" */
