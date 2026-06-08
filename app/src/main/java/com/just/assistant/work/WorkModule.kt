package com.just.assistant.work

import com.just.assistant.usecase.briefing.di.BriefingController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkModule {
    @Binds
    abstract fun bindBriefingController(impl: BriefingScheduler): BriefingController
}
