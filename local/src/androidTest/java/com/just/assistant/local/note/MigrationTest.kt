package com.just.assistant.local.note

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.just.assistant.local.AssistantDatabase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper: MigrationTestHelper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AssistantDatabase::class.java,
            emptyList(),
            FrameworkSQLiteOpenHelperFactory(),
        )

    @Test
    fun migrate_1_to_2_adds_calendarEventId_and_alarmRequestId_columns() {
        val dbName = "migration-test-${System.currentTimeMillis()}"
        helper.createDatabase(dbName, 1).use { db ->
            db.execSQL(
                """
                INSERT INTO notes (id, title, body, type, tags, datetimeIso, createdAtEpochMs, updatedAtEpochMs)
                VALUES (1, 'before', 'body', 'MEMO', '', NULL, 100, 100)
                """.trimIndent(),
            )
        }
        val migratedDb =
            helper.runMigrationsAndValidate(dbName, 2, true, AssistantDatabase.MIGRATION_1_2)
        migratedDb.query("SELECT calendarEventId, alarmRequestId FROM notes WHERE id = 1").use { c ->
            assertEquals(true, c.moveToFirst())
            assertEquals(true, c.isNull(0))
            assertEquals(true, c.isNull(1))
        }
    }

    @Test
    fun migrate_2_to_3_adds_isCompleted_column_with_default_zero() {
        val dbName = "migration-23-test-${System.currentTimeMillis()}"
        helper.createDatabase(dbName, 2).use { db ->
            db.execSQL(
                """
                INSERT INTO notes (id, title, body, type, tags, datetimeIso, createdAtEpochMs, updatedAtEpochMs, calendarEventId, alarmRequestId)
                VALUES (1, 't', 'b', 'MEMO', '', NULL, 100, 100, NULL, NULL)
                """.trimIndent(),
            )
        }
        val migratedDb = helper.runMigrationsAndValidate(dbName, 3, true, AssistantDatabase.MIGRATION_2_3)
        migratedDb.query("SELECT isCompleted FROM notes WHERE id = 1").use { c ->
            assertEquals(true, c.moveToFirst())
            assertEquals(0, c.getInt(0))
        }
    }
}
