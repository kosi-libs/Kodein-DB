
#### 0.8.0 (19-05-2021)

- API
  * Support encryption (see documentation).
  * `org.kodein.db.model.*Primitive` types are deprecated (you should create your won model).
  * Support for multiple value in the same index (which is different from composite value - see documentation).
  * Operation options are now specialized for each operation (`Options.Read` and `Options.Write` are deprecated).
  
- CORE
  * Kotlin 1.5
  * Kodein Memory 0.10.0
  * Kodein Log 0.11.0
  * KotlinX Atomic Fu 0.16.1
  * KotlinX Serialization 1.2.0
  * Windows JNI DLL is now built with Visual Studio.
  
- INTERNALS
  * New version of Index value & ref value storage. This is backward compatible, and previous version is handled correctly.

#### 0.7.0 (08-01-2021)

- API
  * `db.key(vararg id: Any)` is renamed `db.keyById(vararg id: Any)` to align with `getById` & `deleteById`
  * A `Key` can now be serialized as a `Value`, and therefore be part of an index.

#### 0.6.0 (08-01-2021)

- API
  * `Value.ofAscii` is replaced by `Value.of` which supporte UTF-8.

- CORE
  * Kotlin `1.4.31`.
  * KotlinX Serialization `1.1.0`.
  * kodein-Log `0.10.0`.
  * Kodein-Memory `0.7.0`.

#### 0.5.0 (08-01-2021)

- BREAKING CHANGES
  * `Value.ofAny` and `Value.ofAll` are removed in favour of `ModelDB.valueOf` and `ModelDB.valueOfAll` (to allow the use of ValueConverters). This change should only affect middlewares as regular users should use models instead of values.
  * Removed the `Index` type as `Set<Index>` is less correct than `Map<String, Any>` since there can be no two indexes with the same name.
  * Because of the transient dependency to `KotlinX-DateTime` by the new version of `Kodein-Log`, the KotlinX maven repositories is temporarily needed (https://kotlin.bintray.com/kotlinx).

- API
  * `DBListener` now receives `Key<M>` instead of `Key<*>`.
  * Added `DB.getById` & `DB.deleteById`.
  * Introducing `ValueConverter`, which allows to add to the database new types that can be used as values (i.e used in IDs or indexes).

- ANDROID
  * Fixed native compilation.

- CORE
  * Kotlin `1.4.21`.
  * KotlinX Serialization `1.0.1`.
  * kodein-Log `0.8.0`.
  * Kodein-Memory `0.5.0`.

#### 0.4.1 (25-11-2020)

- CORE
  * Kotlin `1.4.20`.
  * KotlinX Serialization `1.0.0`.
  * Kodein-Log `0.7.0`.
  * Kodein-Memory `0.4.1`.

#### 0.4.0 (02-11-2020)

- BREAKING CHANGES
  * JNI for desktop JVM artifacts renamed to `kodein-db-jni-jvm`, `kodein-db-jni-jvm-linux`, `kodein-db-jni-jvm-macos` & `kodein-db-jni-jvm-windows` (added `-jvm-`).
  * `DB.factory` renamed to `DB.default`. Not a breaking change if you are using `DB.open` & `DB.destroy` utilities.

- MODULES
  * new `kodein-leveldb-inmemory` and `kodein-db-inmemory` modules that contain in-memory only backend. DO NOT USE IN PRODUCTION! This is for tests only!

- CORE
  * Kotlin `1.4.10`.

- BUILD
  * Restored Windows build.
