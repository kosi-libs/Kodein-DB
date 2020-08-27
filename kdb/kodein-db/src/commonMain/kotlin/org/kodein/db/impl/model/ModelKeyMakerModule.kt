package org.kodein.db.impl.model

import org.kodein.db.*
import org.kodein.db.ascii.getAscii
import org.kodein.db.data.DataKeyMaker
import org.kodein.db.impl.data.getDocumentKeyType
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.wrap
import kotlin.reflect.KClass
import kotlin.reflect.KType

internal interface ModelKeyMakerModule : KeyMaker {

    val mdb: ModelDBImpl
    val data: DataKeyMaker

    override fun <M : Any> newKey(type: TKType<M>, vararg id: Any) = Key<M>(data.newKey(mdb.getTypeId(mdb.typeTable.getTypeName(type.ktype)), Value.ofAny(id)))

    override fun <M : Any> newKeyFrom(type: TKType<M>, model: M, vararg options: Options.Write) = Key<M>(data.newKey(mdb.getTypeId(mdb.typeTable.getTypeName(type.ktype)), Value.ofAny(mdb.getMetadata(model, options).id)))

    override fun <M : Any> newKeyFromB64(type: TKType<M>, b64: String): Key<M> {
        val key = Key<M>(KBuffer.wrap(Key.b64Decoder.decode(b64)))
        val keyTypeId = getDocumentKeyType(key.bytes)
        val typeName = mdb.typeTable.getTypeName(type.ktype)
        val typeId = mdb.getTypeId(typeName, false)
        check(typeId == keyTypeId) {
            val keyTypeName = mdb.getTypeName(keyTypeId)
            if (keyTypeName != null) "Key is of type ${keyTypeName.getAscii(0)} but was expected to be of type ${typeName.getAscii(0)}. Are you sure this key was created with this Kodein-DB?"
            else "Key is of unknown type, but was expected to be of type ${typeName.getAscii(0)}. Are you sure this key was created with this Kodein-DB?"
        }
        return key
    }
}
