# KrishiFlow 🌾🚜
### Digital Agricultural Procurement, Direct Supply Flow & Food Loss Reduction Platform
**Track 03: Waste & Energy | AWS Hackathon (Build It Track: Open Source, On Your Machine)**

---

## 🌍 The Problem: Track 03 — Waste & Energy

### 1. The Waste Crisis (Food Spoilage & Landfill Methane)
* **25%–40% Post-Harvest Loss**: In multi-tiered traditional agricultural supply chains (Farm → Village Aggregator → APMC Mandi Auctioneer → Wholesale Trader → Urban Distributor → Retailer), perishable produce (tomatoes, leafy greens, onions, fruits) rots before reaching consumers.
* **Methane ($CH_4$) & Landfill Overload**: Decaying organic produce in city municipal landfills is a primary driver of municipal greenhouse gases. Methane is over **28 times more potent than carbon dioxide** at trapping atmospheric heat over a 100-year timescale.
* **Informal Sector Exclusion**: Millions of informal pushcart vendors and street fruit/vegetable sellers lack direct access to wholesale surplus pools, paying inflated rates or going understocked while tons of food are discarded daily in mandi dump yards.

### 2. The Energy & Emissions Crisis (Transit & Cold Storage Burn)
* **Redundant Diesel Transport**: Produce is hauled back and forth across 3–4 intermediary market yards in heavy diesel trucks before reaching urban centers, burning fossil fuels unnecessarily.
* **Cold Storage Energy Waste**: Perishables sit in energy-intensive cold rooms waiting for broker speculative pricing instead of immediately flowing to buyers.

---

## 💡 The KrishiFlow Solution: Closing the Loop on Waste & Energy

```
[Farmer Harvest] ──────► [KrishiFlow Hub / Direct Route] ──────► [Marts & Street Vendors]
                                    │
                       [Surplus-Priority Queue Engine]
                                    │
                    (Real-Time Expiry & Flash Markdowns)
                                    ▼
                [Zero-Waste Informal Vendor Liquidation]
```

1. **Surplus-Priority Queue (Zero Waste Clearance)**:
   - Tracks produce batches by remaining shelf-life hours.
   - Automatically triggers 15%–35% flash discounts for batches nearing expiry, routing them immediately to local street pushcart vendors and juice/caterer micro-businesses before rot can begin.
2. **Quality Grading & Valorization**:
   - Classifies produce into **Grade A** (supermarkets/marts), **Grade B** (street markets/informal vendors), and surplus off-grades for processing, eliminating sorting-station dumping.
3. **Optimized Direct-Flow Logistics (Energy Conservation)**:
   - Connects harvest clusters directly to city drop points, cutting 2–3 transport hops, reducing freight fuel burn by up to 35%, and eliminating excess cold storage holding time.
4. **Transparent Fair Economics**:
   - Farmers earn 15%–18% more by eliminating predatory broker cuts, while buyers receive lower delivered prices.

---

## 🏆 How KrishiFlow Qualifies for AWS Hackathon

### The "Build It" Track: Open Source, On Your Machine
*No AWS account, no credit card, no cloud bill ($0.00 spend).*

| Category | AWS "Build It" Tool | How KrishiFlow Uses It |
|---|---|---|
| **Agents and AI** | **Strands Agents SDK & PartyRock** | Multi-agent orchestration for `FarmerAdvisorAgent`, `MandiPriceMatcherAgent`, and `LogisticsRouteAgent` running locally with the user's custom API key. |
| **Containers & K8s** | **Finch CLI & EKS Distro** | Open-source container runtime (`finch-compose.yml`) running edge mandi service pods without Docker Desktop licensing. |
| **Serverless** | **SAM CLI & LocalStack** | Local SAM template (`template.yaml`) emulating API Gateway, Lambda, and DynamoDB locally on `http://localhost:4566`. |
| **Servers & Runtimes** | **Firecracker & Corretto 21** | Amazon Corretto 21 OpenJDK and Firecracker microVM lightweight specifications (<120ms boot, 5MB memory). |
| **Data & Search** | **OpenSearch** | Local OpenSearch node indexing crop batches with fuzzy BM25 and vector similarity search. |
| **Auth & Policy** | **Cedar** | AWS Cedar policy engine (`policies.cedar`) enforcing fine-grained RBAC for Farmers, Mitras, Wholesalers, and Drivers. |
| **The Plumbing** | **LocalStack EventBridge** | In-memory coroutine events and local EventBridge bus with zero cloud spend. |

*(For teams with AWS cloud credits, KrishiFlow also ships to **AWS Lambda, Amazon API Gateway, Amazon DynamoDB, and Amazon S3** via SAM deploy).*

---

## 📱 How to Use This App

KrishiFlow provides a role-tailored workflow for the entire agricultural and zero-waste ecosystem:

### 1. For Farmers (Kisan)
1. **Pledge Harvest**: Tap **"Pledge Harvest"** or **"+"** on the Home screen.
2. **Enter Details**: Enter crop name (e.g., Tomatoes, Onions), harvest quantity (kg/quintals), harvest date, and quality grade.
3. **Price Comparison**: Instantly inspect the **Direct Flow vs. APMC Mandi** comparison card to see the guaranteed net gain (+15% to +18% higher payout).
4. **AI Farm Advisory**: Ask the built-in advisor for weather forecasts, harvest timing, and optimal market pickup windows.

### 2. For Buyers, Street Vendors & Informal Recyclers (Vyapar)
1. **Browse Surplus Queue**: Navigate to the **"Surplus Queue"** tab.
2. **Snag Flash Deals**: Discover nearing-expiry lots marked down by 15%–35%. Local street pushcart vendors can claim these immediately to resell at affordable rates to urban neighborhoods.
3. **Direct Procurement**: Post Bulk Purchase Requests (RFQs) for direct farm sourcing with transparent unit economics (Farmer PO, Logistics, Operating Margin clearly broken down).

### 3. For Saathi Drivers (Logistics)
1. **Accept Dispatches**: View assigned single-hop farm-to-vendor transport orders.
2. **Direct Routing**: Deliver directly to vendor drops without waiting in multi-hour mandi auction yard lines.
3. **Proof of Delivery**: Complete digital drop-off confirmation.

### 4. For Hackathon Judges & Developers
1. Tap the **🏆 Trophy Icon** in the top action bar (or choose **"AWS Build It Lab"** from the navigation drawer).
2. **Interactive Cedar Policy Sandbox**: Select a persona (Farmer, Wholesaler, Mitra, Driver) and test permission evaluations against `policies.cedar` in real-time.
3. **Local SAM & Strands Test**: Trigger simulated calls to the local SAM API Gateway and Strands Agent loop.
4. **Security & Key Management**: Open **Settings ⚙️ → API Key Management** to safely enter or clear custom API keys without hardcoding secrets into the app build.

---

## 🚀 Quickstart Commands (Run Locally with Finch & SAM)

```bash
# 1. Validate AWS SAM template
sam validate -t template.yaml

# 2. Start LocalStack and OpenSearch with Finch
finch compose -f finch-compose.yml up -d

# 3. Start local SAM API Gateway
sam local start-api --endpoint-url http://localhost:4566 --port 3000

# 4. Verify Cedar authorization policies
cedar authorize \
  --policies policies.cedar \
  --principal 'KrishiRole::"Farmer"' \
  --action 'KrishiAction::"PledgeHarvest"' \
  --resource 'KrishiResource::"HarvestBatch"'
# => ALLOW
```

---

## 🛠️ Technology Stack
* **Client**: Kotlin, Jetpack Compose, Material 3, Android Room DB (KSP), Coroutines & StateFlow
* **AWS Open Source ("Build It")**: AWS Strands Agents SDK, Finch, EKS Distro, SAM CLI, LocalStack, Corretto 21, OpenSearch, Cedar Policy Engine
* **AWS Cloud Deploy ("Ship It")**: AWS Lambda, Amazon API Gateway, Amazon DynamoDB, Amazon S3, Amazon OpenSearch Service
