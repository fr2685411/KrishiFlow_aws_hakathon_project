package com.example.data.repository

import com.example.data.model.ShopProduct
import com.example.data.model.ShopProductCategory
import com.example.data.model.SustainabilityBadge

object SustainableShopCatalog {

    val PRODUCTS: List<ShopProduct> = listOf(
        // -------------------------------------------------------------
        // CATEGORY 1: CERTIFIED SUSTAINABLE SEEDS
        // -------------------------------------------------------------
        ShopProduct(
            id = "seed-chickpea-vikram",
            name = "Phule Vikram Desi Chickpea Seeds",
            hindiName = "फुले विक्रम देसी चना बीज (प्रमाणित)",
            category = ShopProductCategory.CERTIFIED_SEEDS,
            priceRupees = 840.0,
            originalPriceRupees = 1050.0,
            unit = "10 kg bag",
            rating = 4.9,
            reviewsCount = 142,
            manufacturerOrSource = "Mahatma Phule Krishi Vidyapeeth / Mahabeej",
            sustainabilityBadges = listOf(
                SustainabilityBadge.ORGANIC_CERTIFIED,
                SustainabilityBadge.SOIL_REGENERATIVE,
                SustainabilityBadge.ZERO_CHEMICAL_RESIDUE
            ),
            description = "State-certified breeder grade Desi Chana seeds with 96% germination rate. Features deep taproot system for drought tolerance and high nodule-forming efficiency.",
            sustainableFarmingBenefit = "Naturally fixes up to 40 kg atmospheric nitrogen per hectare into your soil, cutting chemical urea fertilizer dependency for subsequent crops.",
            dosageAndUsage = "Sow 30-35 kg per acre with 30cm row spacing; treat with Rhizobium biofertilizer before sowing.",
            targetCropsOrPests = "Rabi Pulses • Drought Resilience • Nitrogen Restitution"
        ),
        ShopProduct(
            id = "seed-soybean-9560",
            name = "JS-9560 Yellow Soybean Seeds",
            hindiName = "JS-9560 पीली सोयाबीन बीज",
            category = ShopProductCategory.CERTIFIED_SEEDS,
            priceRupees = 1950.0,
            originalPriceRupees = 2400.0,
            unit = "30 kg bag",
            rating = 4.8,
            reviewsCount = 98,
            manufacturerOrSource = "ICAR - Indian Institute of Soybean Research",
            sustainabilityBadges = listOf(
                SustainabilityBadge.ORGANIC_CERTIFIED,
                SustainabilityBadge.SOIL_REGENERATIVE,
                SustainabilityBadge.ZERO_CHEMICAL_RESIDUE
            ),
            description = "High germination non-GMO early maturing (90-95 days) soybean variety. Outstanding resistance to pod shattering and stem fly.",
            sustainableFarmingBenefit = "Leaves over 1.2 tonnes of organic root biomass per acre, regenerating depleted soil structure after heavy cereal harvests.",
            dosageAndUsage = "25-30 kg per acre using seed drill with Bradyrhizobium seed coating.",
            targetCropsOrPests = "Kharif Legume • Soil Organic Carbon Restoration"
        ),
        ShopProduct(
            id = "seed-onion-bhima",
            name = "Bhima Dark Red Onion Seeds",
            hindiName = "भीमा डार्क रेड प्याज बीज (ICAR)",
            category = ShopProductCategory.CERTIFIED_SEEDS,
            priceRupees = 1450.0,
            originalPriceRupees = 1800.0,
            unit = "1 kg tin",
            rating = 4.9,
            reviewsCount = 210,
            manufacturerOrSource = "ICAR Directorate of Onion & Garlic Research",
            sustainabilityBadges = listOf(
                SustainabilityBadge.ZERO_CHEMICAL_RESIDUE,
                SustainabilityBadge.ORGANIC_CERTIFIED
            ),
            description = "Original breeder seeds yielding deep red, globe-shaped bulbs with tight skin layers and 4-5 months natural ambient storage longevity.",
            sustainableFarmingBenefit = "High natural resistance to purple blotch and Stemphylium blight, drastically reducing the need for chemical fungicide sprays.",
            dosageAndUsage = "3.5 to 4 kg seeds per acre nursery bed; transplant 45-day seedlings.",
            targetCropsOrPests = "Kharif & Late Kharif High-Yield Commercial Production"
        ),
        ShopProduct(
            id = "seed-moong-ipm",
            name = "IPM-02-03 Desi Green Gram (Moong)",
            hindiName = "IPM-02-03 देसी मूंग बीज (60 दिन)",
            category = ShopProductCategory.CERTIFIED_SEEDS,
            priceRupees = 560.0,
            originalPriceRupees = 720.0,
            unit = "5 kg bag",
            rating = 4.7,
            reviewsCount = 76,
            manufacturerOrSource = "Indian Institute of Pulses Research (IIPR)",
            sustainabilityBadges = listOf(
                SustainabilityBadge.SOIL_REGENERATIVE,
                SustainabilityBadge.WATER_SAVER,
                SustainabilityBadge.ORGANIC_CERTIFIED
            ),
            description = "Rapid 60-day summer/kharif catch crop. High resistance to Yellow Mosaic Virus (YMV) and powdery mildew.",
            sustainableFarmingBenefit = "Perfect biological ground cover that prevents topsoil erosion, suppresses weed emergence organically, and fixes biological nitrogen.",
            dosageAndUsage = "8-10 kg per acre; ideal for intercropping between wider rows of cotton or orchards.",
            targetCropsOrPests = "Summer Catch Crop • Soil Rejuvenation"
        ),

        // -------------------------------------------------------------
        // CATEGORY 2: BIO-PESTICIDES & NATURAL PEST CONTROL (SAFE & SUSTAINABLE)
        // -------------------------------------------------------------
        ShopProduct(
            id = "pest-bioneem-gold",
            name = "BioNeem Gold (Azadirachtin 10,000 PPM)",
            hindiName = "बायोनीम गोल्ड - 10000 PPM शुद्ध नीम अर्क",
            category = ShopProductCategory.BIO_PESTICIDES,
            priceRupees = 620.0,
            originalPriceRupees = 780.0,
            unit = "1 Litre bottle",
            rating = 4.9,
            reviewsCount = 340,
            manufacturerOrSource = "KrishiVeda Bio-Protection Labs",
            sustainabilityBadges = listOf(
                SustainabilityBadge.POLLINATOR_SAFE,
                SustainabilityBadge.ZERO_CHEMICAL_RESIDUE,
                SustainabilityBadge.ORGANIC_CERTIFIED
            ),
            description = "Pure cold-pressed neem kernel oil extract standardized to 10,000 PPM Azadirachtin. Acts as an anti-feedant, repellent, and oviposition deterrent for over 200 pest species.",
            sustainableFarmingBenefit = "Completely safe to honeybees, lacewings, and ladybird beetles. Leaves ZERO toxic chemical residues on fruits and vegetables, enabling premium export quality.",
            dosageAndUsage = "Mix 2 to 2.5 ml per litre of water; spray during evening hours when stomata are open.",
            targetCropsOrPests = "Aphids, Whiteflies, Thrips, Jassids, Leaf Miners, Caterpillars"
        ),
        ShopProduct(
            id = "pest-beauveria-bassiana",
            name = "Beauveria Bassiana Entomopathogenic Bio-Fungus",
            hindiName = "ब्युवेरिया बेसियाना - जैविक कीट भक्षक",
            category = ShopProductCategory.BIO_PESTICIDES,
            priceRupees = 380.0,
            originalPriceRupees = 480.0,
            unit = "1 kg powder pack (2x10^8 CFU/g)",
            rating = 4.8,
            reviewsCount = 185,
            manufacturerOrSource = "National Bio-Center for Agro-Ecology",
            sustainabilityBadges = listOf(
                SustainabilityBadge.POLLINATOR_SAFE,
                SustainabilityBadge.ORGANIC_CERTIFIED,
                SustainabilityBadge.ZERO_CHEMICAL_RESIDUE
            ),
            description = "Natural microbial fungus that parasitizes destructive caterpillars and borers. Spores attach to the pest cuticle, germinate, and eliminate them biologically without chemical neurotoxins.",
            sustainableFarmingBenefit = "Eliminates chemical organophosphate sprays. Breaks pesticide-resistance cycles naturally and persists beneficially in the micro-climate.",
            dosageAndUsage = "5 grams per litre of water or 1 kg per acre in 200L water. Add 0.5 ml jaggery solution as sticking agent.",
            targetCropsOrPests = "Helicoverpa Pod Borer, Spodoptera Armyworm, Diamondback Moth, Dacus Fruit Fly"
        ),
        ShopProduct(
            id = "pest-trichoderma-viride",
            name = "Trichoderma Viride Bio-Fungicide",
            hindiName = "ट्राइकोडर्मा विरिडी - जैविक फफूंदनाशक",
            category = ShopProductCategory.BIO_PESTICIDES,
            priceRupees = 320.0,
            originalPriceRupees = 420.0,
            unit = "1 kg microbial carrier pack",
            rating = 4.9,
            reviewsCount = 290,
            manufacturerOrSource = "ICAR Indian Agricultural Research Institute",
            sustainabilityBadges = listOf(
                SustainabilityBadge.SOIL_REGENERATIVE,
                SustainabilityBadge.ORGANIC_CERTIFIED,
                SustainabilityBadge.ZERO_CHEMICAL_RESIDUE
            ),
            description = "Potent antagonistic biocontrol fungus that colonizes plant roots, physically outcompeting and feeding on harmful soil-borne pathogens like Fusarium, Pythium, and Rhizoctonia.",
            sustainableFarmingBenefit = "Prevents damping-off, collar rot, and bacterial wilt without poisoning soil earthworms or groundwater tables.",
            dosageAndUsage = "Seed treatment: 10g/kg seed; Soil drenching: 2.5 kg mixed with 100 kg compost per acre at planting.",
            targetCropsOrPests = "Root Rot, Collar Rot, Fusarium Wilt, Damping-off in Tomato, Chilli, Onion, Ginger"
        ),
        ShopProduct(
            id = "pest-pheromone-trap-kit",
            name = "Delta Pheromone Trap Kit + 10 Lures",
            hindiName = "फेरोमोन ट्रैप किट + 10 गंध टिकिया (कीट फंदा)",
            category = ShopProductCategory.BIO_PESTICIDES,
            priceRupees = 450.0,
            originalPriceRupees = 600.0,
            unit = "Pack of 5 Traps + 10 Lures",
            rating = 4.7,
            reviewsCount = 112,
            manufacturerOrSource = "EcoTraps Biologicals",
            sustainabilityBadges = listOf(
                SustainabilityBadge.POLLINATOR_SAFE,
                SustainabilityBadge.ZERO_CHEMICAL_RESIDUE
            ),
            description = "Physical weather-resistant delta traps with species-specific female sex pheromone lures. Captures male moths before mating can occur.",
            sustainableFarmingBenefit = "100% pesticide-free monitoring and mass trapping. Zero chemical drift, zero harm to birds and domestic animals.",
            dosageAndUsage = "Hang 4 to 5 traps per acre at crop canopy height; replace lure every 35-45 days.",
            targetCropsOrPests = "Helicoverpa armigera, Spodoptera litura, Tuta absoluta in Vegetables & Pulses"
        ),

        // -------------------------------------------------------------
        // CATEGORY 3: BIO-FERTILIZERS & ORGANIC NUTRIENTS
        // -------------------------------------------------------------
        ShopProduct(
            id = "fert-enriched-vermicompost",
            name = "Enriched Earthworm Vermicompost (केंचुआ खाद)",
            hindiName = "संवर्धित वर्मीकम्पोस्ट (केंचुआ खाद)",
            category = ShopProductCategory.BIO_FERTILIZERS,
            priceRupees = 490.0,
            originalPriceRupees = 650.0,
            unit = "40 kg bag",
            rating = 4.9,
            reviewsCount = 415,
            manufacturerOrSource = "KrishiFlow Rural Women FPO Collective",
            sustainabilityBadges = listOf(
                SustainabilityBadge.SOIL_REGENERATIVE,
                SustainabilityBadge.WATER_SAVER,
                SustainabilityBadge.ORGANIC_CERTIFIED
            ),
            description = "A-grade pure Eisenia fetida earthworm castings enriched with beneficial Azotobacter, Trichoderma, and rock phosphate. Rich in humic acids and active microbial flora.",
            sustainableFarmingBenefit = "Restores depleted topsoil crumb structure, increases water holding capacity by 35%, and reverses chemical fertilizer soil acidity.",
            dosageAndUsage = "Apply 250 to 500 kg per acre in furrows or basins before sowing/transplanting.",
            targetCropsOrPests = "All Vegetables, Pulses, Horticulture Orchards, Cereals"
        ),
        ShopProduct(
            id = "fert-liquid-npk-consortia",
            name = "Bio-NPK Liquid Microbial Consortia",
            hindiName = "बायो-NPK तरल जीवाणु खाद (Rhizobium + PSB + KMB)",
            category = ShopProductCategory.BIO_FERTILIZERS,
            priceRupees = 550.0,
            originalPriceRupees = 700.0,
            unit = "1 Litre bottle",
            rating = 4.8,
            reviewsCount = 230,
            manufacturerOrSource = "Bharat Bio-Fertilizers",
            sustainabilityBadges = listOf(
                SustainabilityBadge.SOIL_REGENERATIVE,
                SustainabilityBadge.ORGANIC_CERTIFIED,
                SustainabilityBadge.ZERO_CHEMICAL_RESIDUE
            ),
            description = "Triple bacterial blend containing atmospheric Nitrogen fixers (Rhizobium/Azotobacter), Phosphate Solubilizing Bacteria (PSB), and Potash Mobilizing Bacteria (KMB).",
            sustainableFarmingBenefit = "Replaces 25-30% of synthetic DAP and Urea. Unlocks insoluble fixed phosphate locked in the soil naturally.",
            dosageAndUsage = "1 Litre per acre via drip fertigation or soil drenching in root zone.",
            targetCropsOrPests = "Replaces Chemical Fertilizers across all Soil Types"
        ),
        ShopProduct(
            id = "fert-cold-seaweed-extract",
            name = "Cold-Water Seaweed Extract + Humic Acid",
            hindiName = "समुद्री शैवाल अर्क + ह्यूमिक एसिड (पौध शक्ति)",
            category = ShopProductCategory.BIO_FERTILIZERS,
            priceRupees = 680.0,
            originalPriceRupees = 850.0,
            unit = "1 Litre concentrated liquid",
            rating = 4.9,
            reviewsCount = 165,
            manufacturerOrSource = "Oceanic Agro Greens",
            sustainabilityBadges = listOf(
                SustainabilityBadge.ORGANIC_CERTIFIED,
                SustainabilityBadge.WATER_SAVER
            ),
            description = "Pure enzymatic extract of Ascophyllum nodosum containing 60+ naturally occurring minerals, cytokinins, auxins, and humic-fulvic substances.",
            sustainableFarmingBenefit = "Provides drought stress resilience, stimulates root elongation, and prevents flower dropping during heatwaves without synthetic plant hormones.",
            dosageAndUsage = "2 ml per litre foliar spray at vegetative, flowering, and fruit-set stages.",
            targetCropsOrPests = "Tomato, Onion, Chilli, Pomegranate, Cotton, Grapes"
        ),

        // -------------------------------------------------------------
        // CATEGORY 4: SOIL BIO-INOCULANTS & MICROBIOME RESTORATION
        // -------------------------------------------------------------
        ShopProduct(
            id = "soil-vam-mycorrhiza",
            name = "VAM Mycorrhiza Bio-Root Inoculant",
            hindiName = "माइकोराइजा (VAM) - जड़ विस्तारक जीवाणु",
            category = ShopProductCategory.SOIL_RESTORATION,
            priceRupees = 420.0,
            originalPriceRupees = 550.0,
            unit = "4 kg granule bucket (100 IP/g)",
            rating = 4.8,
            reviewsCount = 178,
            manufacturerOrSource = "TERI Bio-Tech Innovation",
            sustainabilityBadges = listOf(
                SustainabilityBadge.SOIL_REGENERATIVE,
                SustainabilityBadge.WATER_SAVER,
                SustainabilityBadge.ORGANIC_CERTIFIED
            ),
            description = "Symbiotic endomycorrhizal fungi that physically expand the crop's root absorption surface area by up to 700%, mining deep water and micro-nutrients.",
            sustainableFarmingBenefit = "Saves 25% water by tapping deep sub-surface moisture and makes crops resilient to salinity and drought.",
            dosageAndUsage = "Apply 4 kg per acre near the root zone at the time of sowing or transplanting.",
            targetCropsOrPests = "Root Architecture Builder • All Agricultural Crops"
        ),
        ShopProduct(
            id = "soil-ghana-jeevamrut",
            name = "Ghana Jeevamrut Organic Microbial Pellets",
            hindiName = "घन जीवामृत गोली - देशी गाय जीवाणु खाद",
            category = ShopProductCategory.SOIL_RESTORATION,
            priceRupees = 350.0,
            originalPriceRupees = 450.0,
            unit = "10 kg bag",
            rating = 4.9,
            reviewsCount = 205,
            manufacturerOrSource = "Desi Gau Vigyan Sansthan",
            sustainabilityBadges = listOf(
                SustainabilityBadge.SOIL_REGENERATIVE,
                SustainabilityBadge.ORGANIC_CERTIFIED,
                SustainabilityBadge.ZERO_CHEMICAL_RESIDUE
            ),
            description = "Convenient pelletized formulation of traditional Subhash Palekar natural farming Jeevamrut: fermented Desi cow dung, urine, pulse flour, jaggery, and virgin forest soil.",
            sustainableFarmingBenefit = "Inoculates 100 billion beneficial soil microorganisms per handful, reviving dead compacted chemical soil into living fertile earth.",
            dosageAndUsage = "Broadcase 50-100 kg per acre during field preparation or top-dress near plant base.",
            targetCropsOrPests = "Living Soil Rejuvenation • Zero-Budget Natural Farming (ZBNF)"
        ),

        // -------------------------------------------------------------
        // CATEGORY 5: SUSTAINABLE GEAR, WATER CONSERVATION & MULCH
        // -------------------------------------------------------------
        ShopProduct(
            id = "gear-silver-black-mulch",
            name = "Silver-Black Biodegradable Mulching Film",
            hindiName = "सिल्वर-ब्लैक मल्चिंग फिल्म (25 माइक्रोन)",
            category = ShopProductCategory.SUSTAINABLE_GEAR,
            priceRupees = 1850.0,
            originalPriceRupees = 2250.0,
            unit = "400 Metres Roll (1.2m width)",
            rating = 4.9,
            reviewsCount = 310,
            manufacturerOrSource = "EcoPlast Agro India",
            sustainabilityBadges = listOf(
                SustainabilityBadge.WATER_SAVER,
                SustainabilityBadge.ZERO_CHEMICAL_RESIDUE
            ),
            description = "Dual-color UV-stabilized 25-micron agricultural mulch. Silver upper side reflects excess solar heat and repels flying pests; black underside suppresses 100% weeds.",
            sustainableFarmingBenefit = "Saves 60% irrigation water by eliminating soil evaporation and completely eliminates chemical glyphosate weedicide sprays.",
            dosageAndUsage = "Lay over raised drip-irrigated beds; punch holes for seedlings @ 30-45 cm spacing.",
            targetCropsOrPests = "Tomato, Chilli, Watermelon, Capsicum, Cucumber, Papaya"
        ),
        ShopProduct(
            id = "gear-drip-repair-emitters",
            name = "Pressure-Compensating Drip Emitter & Repair Kit",
            hindiName = "प्रेशर कम्पेंसेटिंग ड्रिप एमिटर व रिपेयर किट",
            category = ShopProductCategory.SUSTAINABLE_GEAR,
            priceRupees = 750.0,
            originalPriceRupees = 950.0,
            unit = "Kit (100 PC Drippers + 20 Joiners + Punch)",
            rating = 4.7,
            reviewsCount = 89,
            manufacturerOrSource = "Jal-Dhara Precision Micro-Irrigation",
            sustainabilityBadges = listOf(
                SustainabilityBadge.WATER_SAVER
            ),
            description = "Self-flushing pressure compensating drippers (4 Litres/Hour) with silicone diaphragm. Ensures uniform water delivery across undulating terrain.",
            sustainableFarmingBenefit = "Guarantees zero water leakage and 95% application efficiency directly to plant root zones, conserving groundwater aquifers.",
            dosageAndUsage = "Insert directly into 16mm or 20mm lateral pipes near tree or plant bases.",
            targetCropsOrPests = "Precision Drip Irrigation • Water Conservation"
        ),
        ShopProduct(
            id = "gear-fruit-protection-bags",
            name = "Micro-Porous Fruit Protection Sleeves",
            hindiName = "सुरक्षात्मक फल बैग (100 पीस) - बिना स्प्रे सुरक्षा",
            category = ShopProductCategory.SUSTAINABLE_GEAR,
            priceRupees = 320.0,
            originalPriceRupees = 400.0,
            unit = "Pack of 100 Non-Woven Sleeves",
            rating = 4.8,
            reviewsCount = 120,
            manufacturerOrSource = "KisanRakshak Agro Textiles",
            sustainabilityBadges = listOf(
                SustainabilityBadge.POLLINATOR_SAFE,
                SustainabilityBadge.ZERO_CHEMICAL_RESIDUE
            ),
            description = "Breathable, water-resistant non-woven fabric sleeves with tie cords. Protects developing individual fruits from fruit flies, moths, hail, and sunburn.",
            sustainableFarmingBenefit = "100% non-chemical physical pest barrier. Guarantees blemish-free fruit without toxic chemical insecticide residue.",
            dosageAndUsage = "Tie around marble-sized fruits after fruit-set; harvest with clean bag intact.",
            targetCropsOrPests = "Pomegranate, Guava, Mango, Dragon Fruit, Apple"
        )
    )
}
