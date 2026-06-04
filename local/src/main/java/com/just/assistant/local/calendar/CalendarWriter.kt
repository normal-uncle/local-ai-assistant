package com.just.assistant.local.calendar

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

data class CalendarEventDraft(
    val title: String,
    val description: String,
    val startEpochMs: Long,
    val endEpochMs: Long,
    val timeZoneId: String = TimeZone.getDefault().id,
)

@Singleton
class CalendarWriter
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun insertEvent(draft: CalendarEventDraft): Long? {
            val calendarId = findDefaultCalendarId() ?: return null
            val values =
                ContentValues().apply {
                    put(CalendarContract.Events.CALENDAR_ID, calendarId)
                    put(CalendarContract.Events.TITLE, draft.title)
                    put(CalendarContract.Events.DESCRIPTION, draft.description)
                    put(CalendarContract.Events.DTSTART, draft.startEpochMs)
                    put(CalendarContract.Events.DTEND, draft.endEpochMs)
                    put(CalendarContract.Events.EVENT_TIMEZONE, draft.timeZoneId)
                }
            val uri: Uri? = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            return uri?.let { ContentUris.parseId(it) }
        }

        fun updateEvent(
            eventId: Long,
            draft: CalendarEventDraft,
        ): Boolean {
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            val values =
                ContentValues().apply {
                    put(CalendarContract.Events.TITLE, draft.title)
                    put(CalendarContract.Events.DESCRIPTION, draft.description)
                    put(CalendarContract.Events.DTSTART, draft.startEpochMs)
                    put(CalendarContract.Events.DTEND, draft.endEpochMs)
                    put(CalendarContract.Events.EVENT_TIMEZONE, draft.timeZoneId)
                }
            return context.contentResolver.update(uri, values, null, null) > 0
        }

        fun deleteEvent(eventId: Long): Boolean {
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            return context.contentResolver.delete(uri, null, null) > 0
        }

        private fun findDefaultCalendarId(): Long? {
            val projection =
                arrayOf(
                    CalendarContract.Calendars._ID,
                    CalendarContract.Calendars.VISIBLE,
                )
            context.contentResolver
                .query(
                    CalendarContract.Calendars.CONTENT_URI,
                    projection,
                    "${CalendarContract.Calendars.VISIBLE} = 1",
                    null,
                    null,
                )
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        return cursor.getLong(0)
                    }
                }
            return null
        }
    }
