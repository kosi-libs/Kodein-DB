//package libleveldb
//
//import kotlinx.cinterop.*
//
//
//interface Comparator {
//    val name: String
//    fun compare(a: ByteArray, b: ByteArray): Int
//}
//
//private class Holder(val comparator: Comparator) {
//    val arena = Arena()
//    val name = comparator.name.cstr.getPointer(arena)
//}
//
//fun Comparator.allocate() = leveldb_comparator_create(
//        StableRef.create(Holder(this)).asCPointer(),
//        staticCFunction { ptr: COpaquePointer? ->
//            val ref = ptr!!.asStableRef<Holder>()
//            ref.get().arena.clear()
//            ref.dispose()
//        },
//        staticCFunction { ptr: COpaquePointer?, aPtr: CPointer<ByteVar>?, aLen: size_t, bPtr: CPointer<ByteVar>?, bLen: size_t ->
//            ptr!!.asStableRef<Holder>().get().comparator.compare(aPtr!!.readBytes(aLen.convert()), bPtr!!.readBytes(bLen.convert()))
//        },
//        staticCFunction { ptr: COpaquePointer? ->
//            ptr!!.asStableRef<Holder>().get().name
//        }
//)
