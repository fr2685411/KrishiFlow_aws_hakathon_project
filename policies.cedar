// ============================================================================
// AWS CEDAR POLICIES FOR KRISHIFLOW
// AWS Hackathon "Build It" Track - Auth and Policy Layer
// Decoupled, formal policy evaluation with zero cloud dependency
// ============================================================================

// 1. FARMER ROLES & HARVEST PLEDGES
// Farmers can pledge harvests, view regional mandi demands, and update their own active lots
permit (
    principal in KrishiRole::"Farmer",
    action in [
        KrishiAction::"PledgeHarvest",
        KrishiAction::"ViewRegionalDemands",
        KrishiAction::"CalculateYieldEstimate"
    ],
    resource is KrishiResource::"HarvestBatch"
);

// Farmers can only cancel or modify harvest lots they personally created
permit (
    principal in KrishiRole::"Farmer",
    action in [
        KrishiAction::"UpdateHarvestDetails",
        KrishiAction::"CancelHarvestLot"
    ],
    resource is KrishiResource::"HarvestBatch"
) when {
    principal.farmerId == resource.ownerId &&
    resource.status == "PENDING_COLLECTION"
};

// 2. KRISHI MITRA (FIELD LEAD / AGRONOMY EXTENSION AGENT)
// Krishi Mitras can inspect harvest quality, assist listing for non-tech farmers, and verify grades
permit (
    principal in KrishiRole::"KrishiMitra",
    action in [
        KrishiAction::"InspectQuality",
        KrishiAction::"VerifyMandiGrade",
        KrishiAction::"AssistFarmerListing",
        KrishiAction::"ApproveQualityScore"
    ],
    resource is KrishiResource::"HarvestBatch"
) when {
    principal.assignedCluster == resource.clusterId
};

// 3. WHOLESALER & MANDI BUYERS (VYAPAR)
// Wholesalers can browse verified produce, place bulk bids, and post regional RFQs
permit (
    principal in KrishiRole::"Wholesaler",
    action in [
        KrishiAction::"BrowseProduceCatalog",
        KrishiAction::"BidSurplusLot",
        KrishiAction::"PostBulkRFQ",
        KrishiAction::"InitiateUPIEscrow"
    ],
    resource is KrishiResource::"MandiOrder"
);

// Wholesalers cannot view confidential farmer reserve minimums below agreed floor
forbid (
    principal in KrishiRole::"Wholesaler",
    action == KrishiAction::"ViewFarmerCostBasis",
    resource is KrishiResource::"HarvestBatch"
);

// 4. SAATHI DRIVERS & FLEET LOGISTICS
// Drivers can view assigned routes, upload photo proof-of-delivery, and log cold-chain temps
permit (
    principal in KrishiRole::"SaathiDriver",
    action in [
        KrishiAction::"AcceptDispatchRoute",
        KrishiAction::"UploadProofOfDelivery",
        KrishiAction::"LogColdChainTemperature",
        KrishiAction::"MarkHubArrival"
    ],
    resource is KrishiResource::"DispatchTrip"
) when {
    principal.vehicleId == resource.assignedVehicleId
};

// 5. CRITICAL SAFEGUARD - ZERO MANDI FLOOR UNDERMINING
// Never allow arbitrary downward override of minimum support price (MSP)
// unless Central Admin triggers declared DisasterRelief protocol
forbid (
    principal,
    action == KrishiAction::"OverrideMandiFloorPrice",
    resource is KrishiResource::"HarvestBatch"
) unless {
    principal has role &&
    principal.role == "CentralAdmin" &&
    context.reason == "DisasterReliefDeclaration"
};
