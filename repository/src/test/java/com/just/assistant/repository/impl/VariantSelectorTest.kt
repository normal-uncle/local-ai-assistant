package com.just.assistant.repository.impl

import com.just.assistant.remote.catalog.dto.VariantDto
import com.just.assistant.repository.model.DeviceProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class VariantSelectorTest {
    private fun v(
        id: String,
        sizeMb: Long,
        minRamGb: Int,
        recommended: Boolean = false,
    ) = VariantDto(
        id = id,
        url = "https://example/$id",
        sha256 = "sha-$id",
        sizeMb = sizeMb,
        minRamGb = minRamGb,
        recommended = recommended,
    )

    @Test
    fun picks_largest_recommended_that_fits_RAM() {
        val variants =
            listOf(
                v("small", 500, 4, recommended = true),
                v("large", 1500, 8, recommended = true),
                v("xlarge", 3000, 12, recommended = true),
            )
        val pick = VariantSelector.select(variants, DeviceProfile(totalRamGb = 8))
        assertEquals("large", pick.id)
    }

    @Test
    fun picks_largest_fitting_if_no_recommended_marked() {
        val variants =
            listOf(
                v("small", 500, 4),
                v("large", 1500, 8),
            )
        val pick = VariantSelector.select(variants, DeviceProfile(totalRamGb = 8))
        assertEquals("large", pick.id)
    }

    @Test
    fun picks_smallest_variant_when_RAM_below_all_minRam() {
        val variants =
            listOf(
                v("medium", 1500, 8),
                v("large", 3000, 12),
            )
        val pick = VariantSelector.select(variants, DeviceProfile(totalRamGb = 4))
        assertEquals("medium", pick.id)
    }

    @Test
    fun picks_smallest_fitting_when_only_one_fits() {
        val variants =
            listOf(
                v("small", 500, 4, recommended = true),
                v("xlarge", 3000, 12, recommended = true),
            )
        val pick = VariantSelector.select(variants, DeviceProfile(totalRamGb = 6))
        assertEquals("small", pick.id)
    }

    @Test
    fun single_variant_catalog_always_picks_it() {
        val variants = listOf(v("only", 500, 1, recommended = true))
        val pick = VariantSelector.select(variants, DeviceProfile(totalRamGb = 12))
        assertEquals("only", pick.id)
    }
}
