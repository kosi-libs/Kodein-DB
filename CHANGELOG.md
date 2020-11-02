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
