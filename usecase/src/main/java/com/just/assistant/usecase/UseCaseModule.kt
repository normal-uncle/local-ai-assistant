package com.just.assistant.usecase

import com.just.assistant.usecase.capture.di.ClassifyCaptureUseCase
import com.just.assistant.usecase.capture.impl.ClassifyCaptureUseCaseImpl
import com.just.assistant.usecase.model.di.EnsureModelDownloadedUseCase
import com.just.assistant.usecase.model.di.ObserveModelDownloadProgressUseCase
import com.just.assistant.usecase.model.di.ObserveModelStatusUseCase
import com.just.assistant.usecase.model.di.ObserveSelectedVariantIdUseCase
import com.just.assistant.usecase.model.impl.EnsureModelDownloadedUseCaseImpl
import com.just.assistant.usecase.model.impl.ObserveModelDownloadProgressUseCaseImpl
import com.just.assistant.usecase.model.impl.ObserveModelStatusUseCaseImpl
import com.just.assistant.usecase.model.impl.ObserveSelectedVariantIdUseCaseImpl
import com.just.assistant.usecase.note.di.FindNoteByIdUseCase
import com.just.assistant.usecase.note.di.ObserveNotesUseCase
import com.just.assistant.usecase.note.di.SaveNoteUseCase
import com.just.assistant.usecase.note.impl.FindNoteByIdUseCaseImpl
import com.just.assistant.usecase.note.impl.ObserveNotesUseCaseImpl
import com.just.assistant.usecase.note.impl.SaveNoteUseCaseImpl
import com.just.assistant.usecase.schedule.di.CancelScheduledItemUseCase
import com.just.assistant.usecase.schedule.di.CompleteScheduledNoteUseCase
import com.just.assistant.usecase.schedule.di.ScheduleEventUseCase
import com.just.assistant.usecase.schedule.di.ScheduleReminderUseCase
import com.just.assistant.usecase.schedule.di.UnscheduleNoteUseCase
import com.just.assistant.usecase.schedule.impl.CancelScheduledItemUseCaseImpl
import com.just.assistant.usecase.schedule.impl.CompleteScheduledNoteUseCaseImpl
import com.just.assistant.usecase.schedule.impl.ScheduleEventUseCaseImpl
import com.just.assistant.usecase.schedule.impl.ScheduleReminderUseCaseImpl
import com.just.assistant.usecase.schedule.impl.UnscheduleNoteUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {
    @Binds
    abstract fun bindSaveNoteUseCase(impl: SaveNoteUseCaseImpl): SaveNoteUseCase

    @Binds
    abstract fun bindObserveNotesUseCase(impl: ObserveNotesUseCaseImpl): ObserveNotesUseCase

    @Binds
    abstract fun bindFindNoteByIdUseCase(impl: FindNoteByIdUseCaseImpl): FindNoteByIdUseCase

    @Binds
    abstract fun bindObserveModelStatusUseCase(impl: ObserveModelStatusUseCaseImpl): ObserveModelStatusUseCase

    @Binds
    abstract fun bindObserveModelDownloadProgressUseCase(
        impl: ObserveModelDownloadProgressUseCaseImpl,
    ): ObserveModelDownloadProgressUseCase

    @Binds
    abstract fun bindEnsureModelDownloadedUseCase(impl: EnsureModelDownloadedUseCaseImpl): EnsureModelDownloadedUseCase

    @Binds
    abstract fun bindObserveSelectedVariantIdUseCase(impl: ObserveSelectedVariantIdUseCaseImpl): ObserveSelectedVariantIdUseCase

    @Binds
    abstract fun bindClassifyCaptureUseCase(impl: ClassifyCaptureUseCaseImpl): ClassifyCaptureUseCase

    @Binds
    abstract fun bindScheduleEventUseCase(impl: ScheduleEventUseCaseImpl): ScheduleEventUseCase

    @Binds
    abstract fun bindScheduleReminderUseCase(impl: ScheduleReminderUseCaseImpl): ScheduleReminderUseCase

    @Binds
    abstract fun bindCancelScheduledItemUseCase(impl: CancelScheduledItemUseCaseImpl): CancelScheduledItemUseCase

    @Binds
    abstract fun bindUnscheduleNoteUseCase(impl: UnscheduleNoteUseCaseImpl): UnscheduleNoteUseCase

    @Binds
    abstract fun bindCompleteScheduledNoteUseCase(impl: CompleteScheduledNoteUseCaseImpl): CompleteScheduledNoteUseCase
}
