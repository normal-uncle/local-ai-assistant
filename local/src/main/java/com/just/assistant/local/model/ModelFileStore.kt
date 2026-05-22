package com.just.assistant.local.model

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelFileStore
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val modelsDir: File
            get() = File(context.filesDir, "models").apply { mkdirs() }

        fun fileFor(variantId: String): File = File(modelsDir, "$variantId.task")

        fun exists(variantId: String): Boolean = fileFor(variantId).exists()

        fun delete(variantId: String): Boolean = fileFor(variantId).delete()

        fun sha256(variantId: String): String? {
            val file = fileFor(variantId)
            if (!file.exists()) return null
            val md = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buf = ByteArray(64 * 1024)
                while (true) {
                    val n = input.read(buf)
                    if (n < 0) break
                    md.update(buf, 0, n)
                }
            }
            return md.digest().joinToString("") { "%02x".format(it) }
        }
    }
