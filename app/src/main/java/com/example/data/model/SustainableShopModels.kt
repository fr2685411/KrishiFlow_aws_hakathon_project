package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class ShopProductCategory(val displayName: String, val hindiName: String) {
    ALL("All Products", "सभी उत्पाद"),
    CERTIFIED_SEEDS("Certified Seeds", "प्रमाणित बीज"),
    BIO_PESTICIDES("Bio-Pesticides", "जैविक कीटनाशक"),
    BIO_FERTILIZERS("Organic Nutrients", "जैविक खाद"),
    SOIL_RESTORATION("Soil Bio-Inoculants", "मृदा संवर्धक"),
    SUSTAINABLE_GEAR("Eco-Gear & Mulch", "जल व सुरक्षा उपकरण")
}

enum class SustainabilityBadge(
    val title: String,
    val hindiTitle: String,
    val colorHex: Long,
    val iconDescription: String
) {
    ORGANIC_CERTIFIED(
        title = "100% Bio-Certified",
        hindiTitle = "प्रमाणित जैविक (NPOP)",
        colorHex = 0xFF15803D,
        iconDescription = "Zero synthetic chemicals; compliant with National Organic standards"
    ),
    POLLINATOR_SAFE(
        title = "Bee & Pollinator Safe",
        hindiTitle = "मधुमक्खी व मित्रकीट सुरक्षित",
        colorHex = 0xFFD97706,
        iconDescription = "Harmless to honeybees, ladybird beetles, and earthworms"
    ),
    ZERO_CHEMICAL_RESIDUE(
        title = "Zero Residue (Toxic-Free)",
        hindiTitle = "रसायन-मुक्त उपज",
        colorHex = 0xFF0D9488,
        iconDescription = "Leaves zero poisonous chemical residues on crops and soil"
    ),
    WATER_SAVER(
        title = "40%+ Water Saver",
        hindiTitle = "जल संरक्षण",
        colorHex = 0xFF0284C7,
        iconDescription = "Cuts evaporation and prevents aquifer depletion"
    ),
    SOIL_REGENERATIVE(
        title = "Soil Microbiome Builder",
        hindiTitle = "मिट्टी जीवाणु पोषक",
        colorHex = 0xFF7C3AED,
        iconDescription = "Multiplies beneficial soil mycorrhiza and organic carbon"
    )
}

data class ShopProduct(
    val id: String,
    val name: String,
    val hindiName: String,
    val category: ShopProductCategory,
    val priceRupees: Double,
    val originalPriceRupees: Double,
    val unit: String,
    val rating: Double,
    val reviewsCount: Int,
    val inStock: Boolean = true,
    val manufacturerOrSource: String,
    val sustainabilityBadges: List<SustainabilityBadge>,
    val description: String,
    val sustainableFarmingBenefit: String,
    val dosageAndUsage: String,
    val targetCropsOrPests: String
)

data class CartItem(
    val product: ShopProduct,
    val quantity: Int
)

data class ShopOrder(
    val orderId: String,
    val items: List<CartItem>,
    val totalAmountRupees: Double,
    val deliveryAddress: String,
    val paymentMode: String = "Cash on Delivery / UPI on Delivery",
    val status: String = "Order Placed - Dispatched from Village Hub",
    val timestamp: Long = System.currentTimeMillis()
)
