package com.example.data.advisory

import com.example.data.model.AgroSeason
import com.example.data.model.CropCategory
import com.example.data.model.CropRecommendation
import com.example.data.model.CropRiskLevel
import com.example.data.model.IrrigationSource
import com.example.data.model.NaturalOrderCategory
import com.example.data.model.NaturalOrderPreservationPlan
import com.example.data.model.PreviousCropType
import com.example.data.model.SoilType
import kotlin.math.roundToInt

/**
 * Agro-Ecological Intelligence Engine for Crop Selection, Yield Forecasting,
 * Risk Mitigation, and Natural Order Preservation.
 *
 * Implements Indian Council of Agricultural Research (ICAR) crop rotation principles
 * to prevent monoculture degradation, rebuild soil organic carbon, and maximize farmer profit.
 */
object CropAdvisoryEngine {

    data class AdvisoryQuery(
        val soilType: SoilType,
        val season: AgroSeason,
        val irrigation: IrrigationSource,
        val previousCrop: PreviousCropType,
        val farmAcres: Double,
        val targetBudgetPerAcre: Double = 18000.0
    )

    private data class CropTemplate(
        val cropName: String,
        val hindiName: String,
        val category: CropCategory,
        val suitableVariety: String,
        val maturityDays: Int,
        val optimalSeasons: Set<AgroSeason>,
        val preferredSoils: Set<SoilType>,
        val minIrrigationRequired: IrrigationSource,
        val baseYieldQuintalsPerAcre: Double,
        val baseCostPerAcre: Double,
        val benchmarkPricePerQuintal: Double,
        val baseWeatherRisk: Int,
        val basePestRisk: Int,
        val baseMarketRisk: Int,
        val naturalOrderCategory: NaturalOrderCategory,
        val preservationPlan: NaturalOrderPreservationPlan
    )

    private val CROP_CATALOG: List<CropTemplate> = listOf(
        // 1. CHICKPEA (Gram / Chana) - Ideal restorative legume
        CropTemplate(
            cropName = "Desi Chickpea (Chana)",
            hindiName = "देसी चना",
            category = CropCategory.PULSES,
            suitableVariety = "Phule Vikram / Vijay / JG-11",
            maturityDays = 100,
            optimalSeasons = setOf(AgroSeason.RABI),
            preferredSoils = setOf(SoilType.BLACK_CLAY, SoilType.ALLUVIAL_LOAM, SoilType.RED_LOAMY),
            minIrrigationRequired = IrrigationSource.RAINFED,
            baseYieldQuintalsPerAcre = 9.5,
            baseCostPerAcre = 12000.0,
            benchmarkPricePerQuintal = 5600.0,
            baseWeatherRisk = 20,
            basePestRisk = 25,
            baseMarketRisk = 18,
            naturalOrderCategory = NaturalOrderCategory.NITROGEN_ENRICHED,
            preservationPlan = NaturalOrderPreservationPlan(
                rotationBenefitSummary = "Symbiotic Rhizobium leguminosarum nodules fix up to 40kg atmospheric Nitrogen/ha, rejuvenating soil depleted by heavy grains.",
                biologicalSoilRemedy = "Inoculate seed with Rhizobium + Phosphate Solubilizing Bacteria (PSB) @ 250g/10kg seeds; apply 5 tonnes decomposed FYM.",
                companionPlantingTrapCrop = "Plant 2 border rows of Mustard or Coriander to repel Helicoverpa pod borer and harbor beneficial predatory wasps.",
                polycultureIntercropRecipe = "Sow Chickpea + Mustard in 6:1 row ratio; or Chickpea + Safflower border for micro-windbreak and soil aeration.",
                waterConservationMethod = "Requires only 2 protective irrigations (flowering and pod-fill); retain previous crop stubble as moisture mulching.",
                naturalPestMitigation = "Install 5 bird perches/acre and Pheromone traps @ 4/acre for pod borer monitoring; spray 5% Neem Seed Kernel Extract (NSKE)."
            )
        ),

        // 2. SOYBEAN - Prime Kharif nitrogen builder
        CropTemplate(
            cropName = "Yellow Soybean",
            hindiName = "पीली सोयाबीन",
            category = CropCategory.PULSES,
            suitableVariety = "JS-335 / JS-9560 / NRC-37",
            maturityDays = 95,
            optimalSeasons = setOf(AgroSeason.KHARIF),
            preferredSoils = setOf(SoilType.BLACK_CLAY, SoilType.ALLUVIAL_LOAM),
            minIrrigationRequired = IrrigationSource.RAINFED,
            baseYieldQuintalsPerAcre = 11.0,
            baseCostPerAcre = 13500.0,
            benchmarkPricePerQuintal = 4800.0,
            baseWeatherRisk = 24,
            basePestRisk = 22,
            baseMarketRisk = 19,
            naturalOrderCategory = NaturalOrderCategory.NITROGEN_ENRICHED,
            preservationPlan = NaturalOrderPreservationPlan(
                rotationBenefitSummary = "Breaks continuous cereal weed cycles, leaves abundant organic root residue (approx. 1.2 tons/acre) for succeeding winter wheat.",
                biologicalSoilRemedy = "Bradyrhizobium japonicum seed inoculation with Trichoderma viride @ 5g/kg seed to prevent seedling rot.",
                companionPlantingTrapCrop = "Border cropping with 4 rows of Pearl Millet (Bajra) to intercept airborne virus vectors (whiteflies/aphids).",
                polycultureIntercropRecipe = "Soybean + Pigeonpea (Arhar) in 4:2 intercrop ratio; optimizes light interception and provides staggered harvest.",
                waterConservationMethod = "Broad Bed & Furrow (BBF) layout ensures drainage during excess monsoon deluges while storing moisture for dry spells.",
                naturalPestMitigation = "Erect yellow sticky traps for whiteflies; apply bio-pesticide Beauveria bassiana @ 1kg/acre at first appearance of defoliators."
            )
        ),

        // 3. NASHIK RED ONION - High demand commercial bulb
        CropTemplate(
            cropName = "Nashik Red Onion",
            hindiName = "नासिक लाल प्याज",
            category = CropCategory.VEGETABLES,
            suitableVariety = "Bhima Super / Bhima Dark Red",
            maturityDays = 115,
            optimalSeasons = setOf(AgroSeason.KHARIF, AgroSeason.RABI, AgroSeason.ZAID),
            preferredSoils = setOf(SoilType.BLACK_CLAY, SoilType.ALLUVIAL_LOAM, SoilType.RED_LOAMY),
            minIrrigationRequired = IrrigationSource.LIMITED_WELL,
            baseYieldQuintalsPerAcre = 90.0,
            baseCostPerAcre = 24000.0,
            benchmarkPricePerQuintal = 2200.0,
            baseWeatherRisk = 32,
            basePestRisk = 30,
            baseMarketRisk = 38,
            naturalOrderCategory = NaturalOrderCategory.REQUIRES_RESTITUTION,
            preservationPlan = NaturalOrderPreservationPlan(
                rotationBenefitSummary = "Shallow fibrous root system extracts surface salts; thrives when following green manured legumes, but must not follow garlic/onion.",
                biologicalSoilRemedy = "Soil application of Mycorrhiza (VAM) @ 4kg/acre with 100kg vermicompost to bolster root uptake of phosphorus.",
                companionPlantingTrapCrop = "Plant Marigold barrier on irrigation borders to repel Thrips tabaci and root lesion nematodes.",
                polycultureIntercropRecipe = "Intercrop border with Coriander or Fenugreek on bed shoulders; provides early cash harvest and protects young seedlings.",
                waterConservationMethod = "Drip irrigation with inline drippers spaced @ 30cm reduces water consumption by 45% and prevents purple blotch fungal spread.",
                naturalPestMitigation = "Blue sticky traps @ 10/acre for thrips monitoring; spray Verticillium lecanii + 0.05% liquid soap during cloudy weather."
            )
        ),

        // 4. HYBRID TOMATO - Premium vegetable requiring biodiversity balance
        CropTemplate(
            cropName = "Hybrid Table Tomato",
            hindiName = "हाइब्रिड टमाटर",
            category = CropCategory.VEGETABLES,
            suitableVariety = "Abhinav / S-312 / Arka Rakshak",
            maturityDays = 120,
            optimalSeasons = setOf(AgroSeason.KHARIF, AgroSeason.RABI, AgroSeason.ZAID),
            preferredSoils = setOf(SoilType.RED_LOAMY, SoilType.ALLUVIAL_LOAM, SoilType.BLACK_CLAY),
            minIrrigationRequired = IrrigationSource.DRIP_MICRO,
            baseYieldQuintalsPerAcre = 210.0,
            baseCostPerAcre = 34000.0,
            benchmarkPricePerQuintal = 1900.0,
            baseWeatherRisk = 35,
            basePestRisk = 40,
            baseMarketRisk = 42,
            naturalOrderCategory = NaturalOrderCategory.HIGH_PEST_CARRYOVER,
            preservationPlan = NaturalOrderPreservationPlan(
                rotationBenefitSummary = "High value crop requiring strict 3-year Solanaceae holiday (no potato/brinjal previously) to break Bacterial Wilt (Ralstonia).",
                biologicalSoilRemedy = "Drench soil with Pseudomonas fluorescens @ 10g/Litre water at transplanting; incorporate 1 ton Neem Cake per acre.",
                companionPlantingTrapCrop = "Plant African Marigold (Tagetes erecta) every 16 rows to act as nematode fatal decoy and attract pollinating bees.",
                polycultureIntercropRecipe = "Alternate border with French Beans (Phaseolus) to supply green nitrogen and shield delicate tomato plants from hot winds.",
                waterConservationMethod = "Silver-black 25-micron reflective mulch film suppresses 90% weeds, cuts evaporation by 60%, and repels aphids.",
                naturalPestMitigation = "Install delta pheromone traps for Tuta absoluta @ 6/acre; spray Bacillus thuringiensis (Bt) @ 2g/L for fruit borer."
            )
        ),

        // 5. GREEN GRAM (Moong) - 60-day rapid ecological restorer
        CropTemplate(
            cropName = "Green Gram (Moong)",
            hindiName = "मूंग दाल",
            category = CropCategory.PULSES,
            suitableVariety = "BM-2003-2 / IPM-02-03 / Samrat",
            maturityDays = 65,
            optimalSeasons = setOf(AgroSeason.KHARIF, AgroSeason.ZAID),
            preferredSoils = setOf(SoilType.ALLUVIAL_LOAM, SoilType.RED_LOAMY, SoilType.SANDY_LOAM, SoilType.BLACK_CLAY),
            minIrrigationRequired = IrrigationSource.RAINFED,
            baseYieldQuintalsPerAcre = 6.8,
            baseCostPerAcre = 9500.0,
            benchmarkPricePerQuintal = 8400.0,
            baseWeatherRisk = 18,
            basePestRisk = 20,
            baseMarketRisk = 15,
            naturalOrderCategory = NaturalOrderCategory.NITROGEN_ENRICHED,
            preservationPlan = NaturalOrderPreservationPlan(
                rotationBenefitSummary = "Rapid 60-day crop cycle; acts as living organic mulch, incorporates 35kg/ha nitrogen, and prevents topsoil erosion between main seasons.",
                biologicalSoilRemedy = "Seed treatment with Rhizobium + PSB culture; foliar spray of Cow Dung-Urine extract (Jeevamrut) at 25 and 40 days.",
                companionPlantingTrapCrop = "Grow 2 rows of Sorghum (Jowar) as outer buffer to disrupt leafhopper flights that transmit Yellow Mosaic Virus.",
                polycultureIntercropRecipe = "Can be intercropped in young orchards (pomegranate/citrus) or planted as summer catch crop before kharif cereals.",
                waterConservationMethod = "Requires only 2-3 light irrigations in summer; zero water stress when following moisture conservation techniques.",
                naturalPestMitigation = "Spray 5% NSKE at flower bud initiation; remove and bury virus-affected plants during first 30 days."
            )
        ),

        // 6. SHARBATI WHEAT - High yield staple for cool winter
        CropTemplate(
            cropName = "Sharbati Wheat",
            hindiName = "शरबती गेहूं",
            category = CropCategory.GRAINS,
            suitableVariety = "GW-322 / HI-1544 (Purna) / Lok-1",
            maturityDays = 115,
            optimalSeasons = setOf(AgroSeason.RABI),
            preferredSoils = setOf(SoilType.BLACK_CLAY, SoilType.ALLUVIAL_LOAM),
            minIrrigationRequired = IrrigationSource.LIMITED_WELL,
            baseYieldQuintalsPerAcre = 18.5,
            baseCostPerAcre = 15000.0,
            benchmarkPricePerQuintal = 2900.0,
            baseWeatherRisk = 22,
            basePestRisk = 16,
            baseMarketRisk = 14,
            naturalOrderCategory = NaturalOrderCategory.REQUIRES_RESTITUTION,
            preservationPlan = NaturalOrderPreservationPlan(
                rotationBenefitSummary = "Exemplary successor to kharif soybean or groundnut; feeds on residual nitrogen nodules and suppresses winter broadleaf weeds.",
                biologicalSoilRemedy = "Apply Azotobacter biofertilizer @ 250g/10kg seed + decomposed FYM enriched with Trichoderma to prevent collar rot.",
                companionPlantingTrapCrop = "Mix 2% Mustard seeds in wheat field or plant 1 row of Mustard after every 9 rows of Wheat for micro-diversity.",
                polycultureIntercropRecipe = "Wheat + Chickpea (8:2 ratio) provides balanced carbohydrate and protein harvest with reciprocal soil protection.",
                waterConservationMethod = "Schedule irrigation strictly at Crown Root Initiation (CRI - 21 days), Tillering, Flowering, and Milking stages.",
                naturalPestMitigation = "Conserve native coccinellid ladybird beetles for biological aphid predation; avoid indiscriminate broad-spectrum sprays."
            )
        ),

        // 7. GREEN CAPSICUM (Shimla Mirch) - High value protected horticulture
        CropTemplate(
            cropName = "Green Capsicum",
            hindiName = "शिमला मिर्च",
            category = CropCategory.VEGETABLES,
            suitableVariety = "Indra / Bachata / Inspiration",
            maturityDays = 125,
            optimalSeasons = setOf(AgroSeason.KHARIF, AgroSeason.RABI),
            preferredSoils = setOf(SoilType.RED_LOAMY, SoilType.ALLUVIAL_LOAM, SoilType.SANDY_LOAM),
            minIrrigationRequired = IrrigationSource.DRIP_MICRO,
            baseYieldQuintalsPerAcre = 140.0,
            baseCostPerAcre = 32000.0,
            benchmarkPricePerQuintal = 3800.0,
            baseWeatherRisk = 28,
            basePestRisk = 34,
            baseMarketRisk = 30,
            naturalOrderCategory = NaturalOrderCategory.HIGH_PEST_CARRYOVER,
            preservationPlan = NaturalOrderPreservationPlan(
                rotationBenefitSummary = "Benefits immensely when following a mustard or brassica crop due to natural soil glucosinolate biofumigation.",
                biologicalSoilRemedy = "Deep soil solarization with clear polythene sheets during May before transplanting; apply 200kg Neem cake/acre.",
                companionPlantingTrapCrop = "Surround plots with 3 rows of tall Maize or Sorghum to block thrips and mites from entering the canopy.",
                polycultureIntercropRecipe = "Intercrop with Coriander or Alyssum flowers along bunds to harbor hoverflies and predatory lacewings.",
                waterConservationMethod = "Fertigation through drip line with organic humic acid and sea-weed extract promotes root architecture and saves 50% water.",
                naturalPestMitigation = "Foliar spray of Neem oil (10,000 ppm) @ 2ml/L alternating with Lecanicillium lecanii for mite control."
            )
        ),

        // 8. GROUNDNUT (Peanut) - Oilseed with underground nitrogen factories
        CropTemplate(
            cropName = "Virginia Groundnut",
            hindiName = "मूंगफली",
            category = CropCategory.OILSEEDS,
            suitableVariety = "JL-24 / Phule Unnati / TG-37A",
            maturityDays = 110,
            optimalSeasons = setOf(AgroSeason.KHARIF, AgroSeason.ZAID),
            preferredSoils = setOf(SoilType.SANDY_LOAM, SoilType.RED_LOAMY, SoilType.ALLUVIAL_LOAM),
            minIrrigationRequired = IrrigationSource.LIMITED_WELL,
            baseYieldQuintalsPerAcre = 14.0,
            baseCostPerAcre = 16000.0,
            benchmarkPricePerQuintal = 6800.0,
            baseWeatherRisk = 22,
            basePestRisk = 24,
            baseMarketRisk = 18,
            naturalOrderCategory = NaturalOrderCategory.NITROGEN_ENRICHED,
            preservationPlan = NaturalOrderPreservationPlan(
                rotationBenefitSummary = "Deep taproot loosens hardpan soil, leaf shedding deposits 800kg/acre humus, and nodule bacteria fix 50kg N/ha.",
                biologicalSoilRemedy = "Treat pods with Rhizobium strain IGR-40 + Trichoderma @ 4g/kg seed; apply Gypsum @ 200kg/acre at pegging stage.",
                companionPlantingTrapCrop = "Plant Castor as trap crop on borders for Spodoptera litura; egg masses can be hand-picked naturally from castor leaves.",
                polycultureIntercropRecipe = "Groundnut + Pearl Millet (Bajra) in 6:1 ratio or Groundnut + Sesame (4:1) for multi-tier canopy architecture.",
                waterConservationMethod = "Light frequent micro-sprinkler or drip irrigation at pegging and pod development stages saves 40% water.",
                naturalPestMitigation = "Erect pheromone traps for Spodoptera litura @ 4/acre; spray nucleopolyhedrovirus (SINPV) @ 100 LE/acre."
            )
        ),

        // 9. BHAGWA POMEGRANATE - Climate resilient arid fruit
        CropTemplate(
            cropName = "Bhagwa Pomegranate",
            hindiName = "भगवा अनार",
            category = CropCategory.FRUITS,
            suitableVariety = "Bhagwa / Super Bhagwa",
            maturityDays = 160,
            optimalSeasons = setOf(AgroSeason.KHARIF, AgroSeason.RABI),
            preferredSoils = setOf(SoilType.RED_LOAMY, SoilType.LATERITE, SoilType.BLACK_CLAY),
            minIrrigationRequired = IrrigationSource.DRIP_MICRO,
            baseYieldQuintalsPerAcre = 60.0,
            baseCostPerAcre = 42000.0,
            benchmarkPricePerQuintal = 8500.0,
            baseWeatherRisk = 26,
            basePestRisk = 32,
            baseMarketRisk = 22,
            naturalOrderCategory = NaturalOrderCategory.RESTORED_PRISTINE,
            preservationPlan = NaturalOrderPreservationPlan(
                rotationBenefitSummary = "Permanent perennial orchard stabilizing soil carbon; enables continuous intercropping of legumes in tree alleyways.",
                biologicalSoilRemedy = "Mulch tree basins with dry sugarcane trash/grass (10cm thick) and apply VAM (Mycorrhiza) + 15kg vermicompost/tree.",
                companionPlantingTrapCrop = "Plant Basil (Tulsi) and Marigold in ring basins to naturally deter fruit flies and sucking insects.",
                polycultureIntercropRecipe = "Intercrop green gram or cowpea in tree avenues during first 3 years to prevent weed growth and generate bonus income.",
                waterConservationMethod = "Double inline drip line with discharge regulated @ 4 LPH; saves 65% water compared to flood irrigation.",
                naturalPestMitigation = "Bag individual fruits with butter paper covers against pomegranate butterfly (Virachola isocrates) and sunburn."
            )
        ),

        // 10. PUSA BOLD MUSTARD - Natural biofumigant oilseed
        CropTemplate(
            cropName = "Pusa Bold Mustard",
            hindiName = "सरसों",
            category = CropCategory.OILSEEDS,
            suitableVariety = "Pusa Bold / Kranti / RH-749",
            maturityDays = 105,
            optimalSeasons = setOf(AgroSeason.RABI),
            preferredSoils = setOf(SoilType.ALLUVIAL_LOAM, SoilType.BLACK_CLAY, SoilType.SANDY_LOAM),
            minIrrigationRequired = IrrigationSource.RAINFED,
            baseYieldQuintalsPerAcre = 8.5,
            baseCostPerAcre = 9000.0,
            benchmarkPricePerQuintal = 5400.0,
            baseWeatherRisk = 20,
            basePestRisk = 28,
            baseMarketRisk = 16,
            naturalOrderCategory = NaturalOrderCategory.BIOFUMIGATED,
            preservationPlan = NaturalOrderPreservationPlan(
                rotationBenefitSummary = "Roots exude glucosinolate compounds that suppress pathogenic soil nematodes and soil-borne fungal sclerotia.",
                biologicalSoilRemedy = "Soil application of elemental Sulphur @ 15kg/acre + Azotobacter biofertilizer to boost oil synthesis and soil health.",
                companionPlantingTrapCrop = "Plant Indian Mustard as trap crop on field perimeter to attract early aphids away from interior plots.",
                polycultureIntercropRecipe = "Mustard + Gram (Chickpea) in 1:6 or Mustard + Lentil (1:5) gives dual oil-pulse ecological synergy.",
                waterConservationMethod = "Deep pre-sowing irrigation followed by only 1 flowering irrigation ensures optimal yield in semi-arid zones.",
                naturalPestMitigation = "Yellow sticky traps @ 8/acre for mustard aphid monitoring; spray Verticillium lecanii or mild soap solution at threshold."
            )
        )
    )

    /**
     * Evaluates all candidates and generates ranked recommendations, yield estimations,
     * risk matrices, and natural order preservation protocols.
     */
    fun evaluateCropPlan(query: AdvisoryQuery): List<CropRecommendation> {
        val scoredList = mutableListOf<CropRecommendation>()

        for (template in CROP_CATALOG) {
            // 1. Calculate Natural Order & Agro-Ecological Compatibility Score (0 - 100)
            var harmonyScore = 50
            val verdictBullets = mutableListOf<String>()

            // Perennial fruit orchard vs seasonal field crop rotation adjustment
            if (template.category == CropCategory.FRUITS) {
                if (query.previousCrop != PreviousCropType.FALLOW_GREEN_MANURE) {
                    harmonyScore -= 15
                    verdictBullets.add("Perennial fruit orchard requires multi-year plantation commitment rather than seasonal field rotation.")
                }
            }

            // Season match
            if (template.optimalSeasons.contains(query.season)) {
                harmonyScore += 15
                verdictBullets.add("Aligned with natural ${query.season.displayName} solar and temperature cycle.")
            } else {
                harmonyScore -= 30
                verdictBullets.add("Off-season planting increases physiological stress and pest susceptibility.")
            }

            // Soil suitability
            if (template.preferredSoils.contains(query.soilType)) {
                harmonyScore += 10
                verdictBullets.add("Soil texture (${query.soilType.displayName}) matches root depth and drainage needs.")
            } else {
                harmonyScore -= 15
                verdictBullets.add("Sub-optimal soil texture may limit aeration or water retention.")
            }

            // Irrigation feasibility
            val irrigationSufficient = when (template.minIrrigationRequired) {
                IrrigationSource.RAINFED -> true
                IrrigationSource.LIMITED_WELL -> query.irrigation != IrrigationSource.RAINFED
                IrrigationSource.DRIP_MICRO -> query.irrigation == IrrigationSource.DRIP_MICRO || query.irrigation == IrrigationSource.CANAL_PERENNIAL
                IrrigationSource.CANAL_PERENNIAL -> query.irrigation == IrrigationSource.CANAL_PERENNIAL
            }
            if (irrigationSufficient) {
                harmonyScore += 10
            } else {
                harmonyScore -= 28
                verdictBullets.add("Water supply is insufficient for crop transpiration; severe drought stress risk.")
            }

            // Natural Order & Crop Rotation Harmony
            when (query.previousCrop.naturalOrderCategory) {
                NaturalOrderCategory.REQUIRES_RESTITUTION -> {
                    if (template.naturalOrderCategory == NaturalOrderCategory.NITROGEN_ENRICHED) {
                        harmonyScore += 25
                        verdictBullets.add("✅ Restorative Rotation: Legume fixes atmospheric nitrogen to heal soil depleted by previous cereals.")
                    } else if (template.naturalOrderCategory == NaturalOrderCategory.REQUIRES_RESTITUTION) {
                        harmonyScore -= 25
                        verdictBullets.add("⚠️ Monoculture Warning: Successive heavy feeders strip soil micronutrients and organic carbon.")
                    }
                }
                NaturalOrderCategory.HIGH_PEST_CARRYOVER -> {
                    if (template.naturalOrderCategory == NaturalOrderCategory.BIOFUMIGATED) {
                        harmonyScore += 25
                        verdictBullets.add("✅ Bio-Sanitizing Rotation: Brassica root exudates cleanse fungal spores left by previous vegetables.")
                    } else if (template.naturalOrderCategory == NaturalOrderCategory.HIGH_PEST_CARRYOVER) {
                        harmonyScore -= 35
                        verdictBullets.add("❌ Severe Ecological Conflict: Consecutive solanaceous crops multiply root-knot nematodes and bacterial wilt exponentially.")
                    } else {
                        harmonyScore += 10
                        verdictBullets.add("Breaks insect pest breeding cycle of previous vegetables.")
                    }
                }
                NaturalOrderCategory.NITROGEN_ENRICHED -> {
                    if (template.naturalOrderCategory == NaturalOrderCategory.REQUIRES_RESTITUTION) {
                        harmonyScore += 20
                        verdictBullets.add("✅ Natural Synergy: Utilizes abundant symbiotic nitrogen nodules left by preceding pulse crop.")
                    } else {
                        harmonyScore += 10
                    }
                }
                NaturalOrderCategory.BIOFUMIGATED -> {
                    harmonyScore += 15
                    verdictBullets.add("Benefiting from biofumigated, sanitized root zone.")
                }
                NaturalOrderCategory.RESTORED_PRISTINE -> {
                    harmonyScore += 18
                    verdictBullets.add("Planting in revitalized, high organic carbon living soil.")
                }
            }

            harmonyScore = harmonyScore.coerceIn(10, 99)

            // 2. Production & Yield Estimation
            val soilMultiplier = if (template.preferredSoils.contains(query.soilType)) 1.05 else 0.85
            val seasonMultiplier = if (template.optimalSeasons.contains(query.season)) 1.0 else 0.75
            val waterMultiplier = if (irrigationSufficient) 1.0 else 0.65
            val acres = query.farmAcres.coerceAtLeast(0.5)

            val expectedYieldPerAcre = template.baseYieldQuintalsPerAcre * soilMultiplier * seasonMultiplier * waterMultiplier
            val totalProductionQuintals = expectedYieldPerAcre * acres
            val totalProductionTonnes = totalProductionQuintals / 10.0

            // 3. Financial Forecast
            val grossRevenue = totalProductionQuintals * template.benchmarkPricePerQuintal
            val inputCost = template.baseCostPerAcre * acres
            val netProfit = grossRevenue - inputCost

            // 4. Dynamic Risk Assessment
            val dynamicWeatherRisk = (template.baseWeatherRisk * (if (query.irrigation == IrrigationSource.RAINFED) 1.5 else 0.8)).roundToInt().coerceIn(10, 95)
            val pestMultiplier = if (query.previousCrop.naturalOrderCategory == NaturalOrderCategory.HIGH_PEST_CARRYOVER && template.naturalOrderCategory == NaturalOrderCategory.HIGH_PEST_CARRYOVER) 1.7 else 0.85
            val dynamicPestRisk = (template.basePestRisk * pestMultiplier).roundToInt().coerceIn(10, 95)
            val dynamicMarketRisk = template.baseMarketRisk.coerceIn(10, 95)
            val waterDepletionRisk = if (query.irrigation == IrrigationSource.DRIP_MICRO) 15 else if (query.irrigation == IrrigationSource.RAINFED) 65 else 40

            val compositeRiskScore = (dynamicWeatherRisk * 0.35 + dynamicPestRisk * 0.35 + dynamicMarketRisk * 0.30).roundToInt()
            val overallRisk = when {
                compositeRiskScore <= 28 -> CropRiskLevel.LOW
                compositeRiskScore <= 50 -> CropRiskLevel.MODERATE
                else -> CropRiskLevel.HIGH
            }

            scoredList.add(
                CropRecommendation(
                    cropName = template.cropName,
                    hindiName = template.hindiName,
                    category = template.category,
                    suitableVariety = template.suitableVariety,
                    maturityDays = template.maturityDays,
                    naturalHarmonyScore = harmonyScore,
                    naturalHarmonyVerdict = verdictBullets.joinToString(" "),
                    expectedYieldPerAcreQuintals = (expectedYieldPerAcre * 10).roundToInt() / 10.0,
                    totalEstimatedProductionQuintals = (totalProductionQuintals * 10).roundToInt() / 10.0,
                    totalEstimatedProductionTonnes = (totalProductionTonnes * 100).roundToInt() / 100.0,
                    expectedMarketRatePerQuintal = template.benchmarkPricePerQuintal,
                    estimatedGrossRevenueRupees = grossRevenue,
                    estimatedInputCostRupees = inputCost,
                    estimatedNetProfitRupees = netProfit,
                    overallRiskLevel = overallRisk,
                    weatherRiskPercent = dynamicWeatherRisk,
                    pestRiskPercent = dynamicPestRisk,
                    marketGlutRiskPercent = dynamicMarketRisk,
                    waterDepletionRiskPercent = waterDepletionRisk,
                    naturalOrderPreservationPlan = template.preservationPlan
                )
            )
        }

        // Rank primarily by natural harmony score, then by net profit
        return scoredList.sortedWith(
            compareByDescending<CropRecommendation> { it.naturalHarmonyScore }
                .thenByDescending { it.estimatedNetProfitRupees }
        )
    }
}
