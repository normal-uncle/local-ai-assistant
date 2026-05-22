package com.just.assistant.local.model

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModelFileStoreTest {
    private lateinit var store: ModelFileStore

    @Before
    fun setUp() {
        store = ModelFileStore(ApplicationProvider.getApplicationContext())
        store.delete("test-variant")
    }

    @After fun tearDown() {
        store.delete("test-variant")
    }

    @Test
    fun fileFor_returns_consistent_path_under_models_dir() {
        val f1 = store.fileFor("test-variant")
        val f2 = store.fileFor("test-variant")
        assertEquals(f1.absolutePath, f2.absolutePath)
        assertTrue(f1.parentFile!!.name == "models")
    }

    @Test
    fun exists_is_false_when_no_file() {
        assertFalse(store.exists("test-variant"))
    }

    @Test
    fun exists_is_true_after_write() {
        store.fileFor("test-variant").writeText("dummy")
        assertTrue(store.exists("test-variant"))
    }

    @Test
    fun sha256_returns_null_for_missing_and_value_for_present() {
        assertNull(store.sha256("test-variant"))
        store.fileFor("test-variant").writeBytes(ByteArray(10) { it.toByte() })
        val sha = store.sha256("test-variant")
        assertNotNull(sha)
        assertEquals("1f825aa2f0020ef7cf91dfa30da4668d791c5d4824fc8e41354b89ec05795ab3", sha)
    }

    @Test
    fun delete_removes_file() {
        store.fileFor("test-variant").writeText("x")
        assertTrue(store.delete("test-variant"))
        assertFalse(store.exists("test-variant"))
    }
}
