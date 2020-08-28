package org.kodein.db.impl.model

import org.kodein.db.impl.model.jvm.AnnotationMetadataExtractor
import org.kodein.db.model.Id
import org.kodein.db.model.Indexed
import org.kodein.db.model.orm.DefaultSerializer
import org.kodein.db.orm.kryo.KryoSerializer
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.array
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@Suppress("ClassName")
class ModelDBJvmTests_00_MetadataAnnotations : ModelDBTests() {

    override fun testSerializer(): DefaultSerializer = KryoSerializer()

    @Test
    fun test00_MetadataAnnotations() {
        val adult = AAdult(42, "Salomon", "BRYS", Date(15, 12, 1986))
        val metadata = AnnotationMetadataExtractor().extractMetadata(adult)
        assertEquals(42, metadata?.id)
    }

    class Test01()

    @Test
    fun test01_NoId() {
        assertNull(AnnotationMetadataExtractor().extractMetadata(Test01()))
    }

    abstract class Test02_A {
        @Id val aId: Int = 42
    }

    class Test02_C : Test02_A() {
        @Id val id: String = "_"
    }

    @Test
    fun test02_MultipleIds() {
        val ex = assertFailsWith<IllegalStateException> { AnnotationMetadataExtractor().extractMetadata(Test02_C()) }
        assertEquals("class org.kodein.db.impl.model.ModelDBJvmTests_00_MetadataAnnotations\$Test02_C has two IDs: private final int org.kodein.db.impl.model.ModelDBJvmTests_00_MetadataAnnotations\$Test02_A.aId AND private final java.lang.String org.kodein.db.impl.model.ModelDBJvmTests_00_MetadataAnnotations\$Test02_C.id", ex.message)
    }

    abstract class Test03_A {
        @Id val id: Int = 42
        @Indexed("name") val nameF = "me"
    }

    class Test03_C : Test03_A() {
        @Indexed("name") fun nameM() = "also me"
    }

    @Test
    fun test03_MultipleSameIndex() {
        val ex = assertFailsWith<IllegalStateException> { AnnotationMetadataExtractor().extractMetadata(Test03_C()) }
        assertEquals("class org.kodein.db.impl.model.ModelDBJvmTests_00_MetadataAnnotations\$Test03_C has indexes that are defined twice.\n    name: private final java.lang.String org.kodein.db.impl.model.ModelDBJvmTests_00_MetadataAnnotations\$Test03_A.nameF AND public final java.lang.String org.kodein.db.impl.model.ModelDBJvmTests_00_MetadataAnnotations\$Test03_C.nameM()\n", ex.message)
    }

    class Test04 {
        @Id val id = "id"
        @Indexed("name") val nameF = "me"
        @Indexed("name") fun nameM() = "also me"
    }

    @Test
    fun test04_MultipleSameIndexInSameClass() {
        val ex = assertFailsWith<IllegalStateException> { AnnotationMetadataExtractor().extractMetadata(Test04()) }
        assertEquals("class org.kodein.db.impl.model.ModelDBJvmTests_00_MetadataAnnotations\$Test04 has indexes that are defined twice.\n    name: public final java.lang.String org.kodein.db.impl.model.ModelDBJvmTests_00_MetadataAnnotations\$Test04.nameM() AND  private final java.lang.String org.kodein.db.impl.model.ModelDBJvmTests_00_MetadataAnnotations\$Test04.nameF\n", ex.message)
    }
}
