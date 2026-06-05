package com.just.assistant.local.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface ImageStore {
    /** [source] 이미지를 다운스케일 후 내부 저장소(filesDir/images)에 복사, 저장된 file:// uri 반환. */
    suspend fun persist(source: Uri): Uri

    /** 분류 추론용 다운스케일 JPEG 바이트. */
    suspend fun toClassifierBytes(source: Uri): ByteArray
}

@Singleton
class ImageStoreImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : ImageStore {
        // persist는 우리 앱 자체 filesDir에 저장하고 그 file:// uri를 돌려준다 (Coil이 file:// 로드 가능).
        // FileProvider content uri는 다른 앱 노출용이라 여기선 불필요 — 카메라 출력(Task 11)에만 쓰인다.
        override suspend fun persist(source: Uri): Uri =
            withContext(Dispatchers.IO) {
                val bytes = downscaleToJpeg(source)
                val dir = File(context.filesDir, "images").apply { mkdirs() }
                val file = File(dir, "${UUID.randomUUID()}.jpg")
                file.outputStream().use { it.write(bytes) }
                Uri.fromFile(file)
            }

        override suspend fun toClassifierBytes(source: Uri): ByteArray =
            withContext(Dispatchers.IO) { downscaleToJpeg(source) }

        private fun downscaleToJpeg(source: Uri): ByteArray {
            val bitmap = decodeScaled(source, MAX_EDGE_PX)
            return ByteArrayOutputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
                bitmap.recycle()
                out.toByteArray()
            }
        }

        private fun decodeScaled(source: Uri, maxEdge: Int): Bitmap {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(source).use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            var sample = 1
            val longest = maxOf(bounds.outWidth, bounds.outHeight)
            while (longest / sample > maxEdge) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            val decoded =
                context.contentResolver.openInputStream(source).use {
                    BitmapFactory.decodeStream(it, null, opts)
                } ?: error("이미지 디코드 실패: $source")
            val edge = maxOf(decoded.width, decoded.height)
            if (edge <= maxEdge) return decoded
            val scale = maxEdge.toFloat() / edge
            val scaled =
                Bitmap.createScaledBitmap(
                    decoded,
                    (decoded.width * scale).toInt(),
                    (decoded.height * scale).toInt(),
                    true,
                )
            if (scaled != decoded) decoded.recycle()
            return scaled
        }

        companion object {
            private const val MAX_EDGE_PX = 768
            private const val JPEG_QUALITY = 85
        }
    }
