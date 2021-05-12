package org.kodein.db.impl.model

import kotlinx.serialization.Serializable
import org.kodein.db.*
import org.kodein.db.encryption.DBFeatureDisabledError
import org.kodein.db.model.associatedObject
import org.kodein.db.model.findAllByIndex
import org.kodein.db.model.findByIndex
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.use
import kotlin.test.*

@Suppress("ClassName")
abstract class ModelDBTests_03_Indexes : ModelDBTests() {

    class LDB : ModelDBTests_03_Indexes(), ModelDBTests.LDB
    class IM : ModelDBTests_03_Indexes(), ModelDBTests.IM

    abstract class Encrypted : ModelDBTests_03_Indexes(), ModelDBTests.Encrypted {
        @Test override fun test02_FindByIndexOpen() { assertFailsWith<DBFeatureDisabledError> { super.test02_FindByIndexOpen() } }
        @Test override fun test04_FindNothingByIndexOpen() { assertFailsWith<DBFeatureDisabledError> { super.test04_FindNothingByIndexOpen() } }

        class LDB : Encrypted(), ModelDBTests.LDB
        class IM : Encrypted(), ModelDBTests.IM
    }


    @Test
    fun test00_FindAllByIndex() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val notMe = Adult("Salomon", "MALHANGU", Date(10, 7, 1956))
        val laila = Adult("Laila", "BRYS", Date(25, 8, 1989))
        val meKey = mdb.put(me).key
        val notMeKey = mdb.put(notMe).key
        val lailaKey = mdb.put(laila).key

        mdb.findAllByIndex<Adult>("firstName").use { cursor ->
            assertCursorIs(cursor) {
                lailaKey {
                    assertEquals(laila, it.model().model)
                    assertNotSame(laila, it.model().model)
                }
                meKey {
                    assertEquals(me, it.model().model)
                    assertNotSame(me, it.model().model)
                }
                notMeKey {
                    assertEquals(notMe, it.model().model)
                    assertNotSame(notMe, it.model().model)
                }
            }
        }
    }

    @Test
    fun test01_FindByIndexValue() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val notMe = Adult("Salomon", "MALHANGU", Date(10, 7, 1956))
        val laila = Adult("Laila", "BRYS", Date(25, 8, 1989))
        val meKey = mdb.put(me).key
        val notMeKey = mdb.put(notMe).key
        mdb.put(laila)

        mdb.findByIndex<Adult>("firstName", "Salomon").use { cursor ->
            assertCursorIs(cursor) {
                meKey {
                    assertEquals(me, it.model().model)
                    assertNotSame(me, it.model().model)
                }
                notMeKey {
                    assertEquals(notMe, it.model().model)
                    assertNotSame(notMe, it.model().model)
                }
            }
        }
    }

    @Test
    open fun test02_FindByIndexOpen() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val notMe = Adult("Salomon", "MALHANGU", Date(10, 7, 1956))
        val laila = Adult("Laila", "BRYS", Date(25, 8, 1989))
        val sarah = Adult("Sarah", "Bernhardt", Date(23, 10, 1844))
        val meKey = mdb.put(me).key
        val notMeKey = mdb.put(notMe).key
        mdb.put(laila)
        val sarahKey = mdb.put(sarah).key

        mdb.findByIndex<Adult>("firstName", "Sa", isOpen = true).use { cursor ->
            assertCursorIs(cursor) {
                meKey {
                    assertEquals(me, it.model().model)
                    assertNotSame(me, it.model().model)
                }
                notMeKey {
                    assertEquals(notMe, it.model().model)
                    assertNotSame(notMe, it.model().model)
                }
                sarahKey {
                    assertEquals(sarah, it.model().model)
                    assertNotSame(sarah, it.model().model)
                }
            }
        }
    }

    @Test
    fun test03_FindNothingByIndex() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val laila = Adult("Laila", "BRYS", Date(25, 8, 1989))
        mdb.put(me)
        mdb.put(laila)

        mdb.findByIndex<Adult>("firstName", "Roger").use {
            assertFalse(it.isValid())
        }
    }

    @Test
    open fun test04_FindNothingByIndexOpen() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val laila = Adult("Laila", "BRYS", Date(25, 8, 1989))
        mdb.put(me)
        mdb.put(laila)

        mdb.findByIndex<Adult>("firstName", "R", isOpen = true).use {
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test05_getIndexes() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        mdb.put(me)

        val indexes = mdb.getIndexesOf(mdb.keyById<Adult>(Value.of("BRYS", "Salomon"))).toSet()
        assertEquals(setOf("birth", "firstName"), indexes)
    }

    @Serializable
    data class T06_Parent(override val id: String, val child: Key<T06_Child>) : Metadata {
        override fun indexes(): Map<String, Any> = mapOf("child" to child)
    }

    @Serializable
    data class T06_Child(override val id: String) : Metadata

    @Test
    fun test06_keyIndex() {
        val child = T06_Child("C")
        val parent = T06_Parent("P", mdb.keyById(child.id))

        mdb.put(parent)
        val retreived = mdb.findByIndex<T06_Parent>("child", mdb.keyFrom(child)).use { it.model().model }

        assertEquals(parent, retreived)
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun test01_FindByIndexMultipleValue() {
        val vh1 = mdb.put(Text("vh1", "Si l’on ne peut pardonner, cela ne vaut pas la peine de vaincre.", listOf("pouvoir" to 11, "pardon" to 16, "valoir" to 35, "peine" to 47, "vaincre" to 56))).key
        val vh2 = mdb.put(Text("vh2", "Nul n’ira jusqu’au fond du rire d’un enfant.", listOf("aller" to 6, "fond" to 19, "rire" to 27, "enfant" to 37))).key
        val vh3 = mdb.put(Text("vh3", "Chaque enfant qu'on enseigne est un homme qu'on gagne.", listOf("enfant" to 7, "enseigner" to 20, "homme" to 36, "gagner" to 48))).key
        val vh4 = mdb.put(Text("vh4", "Sauvons la liberté, la liberté sauve le reste.", listOf("sauver" to 0, "liberté" to 11, "reste" to 40))).key
        val vh5 = mdb.put(Text("vh5", "Tout ce qui augmente la liberté augmente la responsabilité.", listOf("tout" to 0, "augmenter" to 12, "liberté" to 24, "responsabilité" to 44))).key
        val vh6 = mdb.put(Text("vh6", "Rien n'est stupide comme vaincre ; la vraie gloire est convaincre.", listOf("rien" to 0, "stupide" to 11, "vaincre" to 25, "vrai" to 38, "gloire" to 44, "convaincre" to 55))).key
        val vh7 = mdb.put(Text("vh7", "L'éclat de rire est la dernière ressource de la rage et du désespoir.", listOf("éclat" to 2, "rire" to 11, "dernier" to 23, "resource" to 32, "rage" to 48, "désespoir" to 59))).key
        val vh8 = mdb.put(Text("vh8", "Qui n’est pas capable d’être pauvre n’est pas capable d’être libre.", listOf("capacité" to 14, "être" to 24, "pauvreté" to 29, "liberté" to 61))).key

        mdb.findByIndex<Text>("tokens", "enfant").use { cursor ->
            assertCursorIs(cursor) {
                vh2 {
                    assertEquals(37, it.associatedObject())
                }
                vh3 {
                    assertEquals(7, it.associatedObject())
                }
            }
        }

        mdb.findByIndex<Text>("tokens", "rire").use { cursor ->
            assertCursorIs(cursor) {
                vh2 {
                    assertEquals(27, it.associatedObject())
                }
                vh7 {
                    assertEquals(11, it.associatedObject())
                }
            }
        }
    }

}
