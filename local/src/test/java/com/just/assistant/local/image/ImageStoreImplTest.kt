package com.just.assistant.local.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class ImageStoreImplTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    private fun writeLargeImage(): Uri {
        val bmp = Bitmap.createBitmap(2000, 1500, Bitmap.Config.ARGB_8888)
        val file = File(context.cacheDir, "big.jpg")
        file.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        return Uri.fromFile(file)
    }

    @Test
    fun classifier_bytes_are_downscaled_under_max_edge() =
        runTest {
            val store = ImageStoreImpl(context)
            val bytes = store.toClassifierBytes(writeLargeImage())
            val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            assertTrue(maxOf(decoded.width, decoded.height) <= 768)
        }

    @Test
    fun persist_writes_file_into_images_dir() =
        runTest {
            val store = ImageStoreImpl(context)
            store.persist(writeLargeImage())
            val dir = File(context.filesDir, "images")
            assertTrue(dir.exists() && (dir.listFiles()?.isNotEmpty() == true))
        }
}
