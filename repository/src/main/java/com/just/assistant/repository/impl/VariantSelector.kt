package com.just.assistant.repository.impl

import com.just.assistant.remote.catalog.dto.VariantDto
import com.just.assistant.repository.model.DeviceProfile

object VariantSelector {
    /**
     * 디바이스의 RAM이 만족하는 변종 중 가장 큰(=가장 메모리를 많이 요구하는) recommended를 선택.
     * recommended 없으면 만족하는 가장 큰 일반 변종. 만족하는 변종이 하나도 없으면 sizeMb가 가장 작은 변종(저사양 fallback).
     */
    fun select(
        variants: List<VariantDto>,
        profile: DeviceProfile,
    ): VariantDto {
        require(variants.isNotEmpty()) { "no variants in catalog" }
        val fitting = variants.filter { it.minRamGb <= profile.totalRamGb }
        return if (fitting.isNotEmpty()) {
            fitting.filter { it.recommended }.maxByOrNull { it.sizeMb }
                ?: fitting.maxByOrNull { it.sizeMb }!!
        } else {
            variants.minByOrNull { it.sizeMb }!!
        }
    }
}
