//package libleveldb
//
//import kotlinx.cinterop.*
//import platform.posix.size_t
//
//
//class Options {
//
//    private val ptr = leveldb_options_create()
//
//    fun setComparator(comparator: Comparator) = leveldb_options_set_comparator(ptr,
//
//    )
//
//    fun destroy() = leveldb_options_destroy(ptr)
//}
