package com.example.data.repository

import com.example.data.model.CartItem
import com.example.data.model.ShopProductCategory
import com.example.data.model.SustainabilityBadge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SustainableShopTest {

    @Test
    fun testCatalogHasSustainableProductsAcrossAllCategories() {
        val products = SustainableShopCatalog.PRODUCTS
        assertTrue("Shop catalog should have products", products.isNotEmpty())

        val seeds = products.filter { it.category == ShopProductCategory.CERTIFIED_SEEDS }
        val bioPesticides = products.filter { it.category == ShopProductCategory.BIO_PESTICIDES }
        val bioFertilizers = products.filter { it.category == ShopProductCategory.BIO_FERTILIZERS }
        val soilInoculants = products.filter { it.category == ShopProductCategory.SOIL_RESTORATION }
        val ecoGear = products.filter { it.category == ShopProductCategory.SUSTAINABLE_GEAR }

        assertTrue("Should have certified seeds", seeds.isNotEmpty())
        assertTrue("Should have bio-pesticides", bioPesticides.isNotEmpty())
        assertTrue("Should have bio-fertilizers", bioFertilizers.isNotEmpty())
        assertTrue("Should have soil bio-inoculants", soilInoculants.isNotEmpty())
        assertTrue("Should have eco-gear/mulch", ecoGear.isNotEmpty())
    }

    @Test
    fun testProductsHaveSustainabilityGuarantees() {
        val products = SustainableShopCatalog.PRODUCTS
        for (product in products) {
            assertTrue("Every product must have sustainability badges", product.sustainabilityBadges.isNotEmpty())
            assertTrue("Product ${product.name} must have sustainable farming benefit description", product.sustainableFarmingBenefit.isNotBlank())
            assertTrue("Product ${product.name} price must be positive", product.priceRupees > 0)
        }

        // Check specific bio-pesticide sustainability
        val bioneem = products.first { it.id == "pest-bioneem-gold" }
        assertTrue("BioNeem should be pollinator safe", bioneem.sustainabilityBadges.contains(SustainabilityBadge.POLLINATOR_SAFE))
        assertTrue("BioNeem should be zero chemical residue", bioneem.sustainabilityBadges.contains(SustainabilityBadge.ZERO_CHEMICAL_RESIDUE))

        // Check certified seeds
        val chickpea = products.first { it.id == "seed-chickpea-vikram" }
        assertTrue("Chickpea seeds should be soil regenerative", chickpea.sustainabilityBadges.contains(SustainabilityBadge.SOIL_REGENERATIVE))
    }

    @Test
    fun testCartCalculationAndEcoSubsidy() {
        val products = SustainableShopCatalog.PRODUCTS
        val p1 = products[0] // e.g. Phule Vikram Desi Chana @ 840.0
        val p2 = products[4] // e.g. BioNeem Gold @ 620.0

        val cart = listOf(
            CartItem(product = p1, quantity = 2), // 840 * 2 = 1680
            CartItem(product = p2, quantity = 1)  // 620 * 1 = 620
        )

        val subtotal = cart.sumOf { it.product.priceRupees * it.quantity }
        assertEquals(2300.0, subtotal, 0.01)

        val ecoDiscount = if (subtotal >= 1000.0) 100.0 else 0.0
        val finalPayable = subtotal - ecoDiscount
        assertEquals(2200.0, finalPayable, 0.01)
    }
}
