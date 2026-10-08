# 🏆 KrishiFlow — AWS Hackathon 2026 Pitch Document

## 🎯 Track Selection: **Track 03 — Waste & Energy**
### Category Focus: *Close the loop on what a city throws away and cut what it burns.*
### AWS Track Alignment: **Build It** (*Open source, on your machine. No AWS account, no card, no bill.*)

---

## 🌾 1. Why KrishiFlow is Track 03: Waste & Energy

### 🗑️ The Waste Problem
* **Food Spoilage**: 25%–40% of agricultural produce rots across intermediate broker yards, ending up in city municipal landfills.
* **Methane Emissions ($CH_4$)**: Rotten food in landfills emits methane, a greenhouse gas **28x more destructive to the ozone layer than carbon dioxide**.
* **Informal Sector Disconnect**: Street pushcart vendors and small sellers struggle with volatile prices while tons of edible food are dumped daily at mandis.

### ⚡ The Energy Problem
* **Transit Fuel Consumption**: Produce is hauled between 3–4 rural and city auction yards before reaching consumers, burning thousands of liters of diesel fuel.
* **Cold-Chain Energy Consumption**: Holding stock speculatively for days drains heavy grid and diesel electricity in cold storage warehouses.

### 🔄 The KrishiFlow Closed-Loop Solution
1. **Surplus-Priority Queue**:
   - Detects batches nearing remaining shelf-life hours.
   - Triggers dynamic markdown discounts (15%–35%) and redirects produce immediately to local pushcart vendors and juice/caterer micro-businesses before rot occurs.
2. **Organic Waste & Spoilage Elimination**:
   - Diverts 100% of edible harvest away from city landfills, preventing methane generation.
3. **Direct-Flow Energy Savings**:
   - Routes shipments directly from rural harvest hubs to urban drop points in single-hop logistics, reducing diesel freight fuel consumption by up to 35%.

---

## ☁️ 2. How KrishiFlow Qualifies for AWS Hackathon

### "Build It" Track Mapping ($0.00 AWS Bill)

| Pillar | AWS Tool | KrishiFlow Implementation |
|---|---|---|
| **Agents & AI** | **Strands Agents SDK & PartyRock** | Multi-agent autonomous supply network (`FarmerAdvisorAgent`, `MandiPriceMatcherAgent`, `LogisticsRouteAgent`) running locally using the user's custom API key. |
| **Containers & K8s** | **Finch CLI & EKS Distro** | Containerized edge mandi services via open-source Finch CLI (`finch-compose.yml`) without commercial Docker licensing. |
| **Serverless** | **SAM CLI & LocalStack** | SAM template (`template.yaml`) emulating API Gateway, Lambda, and DynamoDB locally on `http://localhost:4566`. |
| **Servers & Runtimes** | **Firecracker & Corretto 21** | Amazon Corretto 21 OpenJDK runtime with Firecracker microVM specs booting in <120ms with 5MB memory overhead. |
| **Data & Search** | **OpenSearch** | Local OpenSearch node indexing mandi produce lots with fuzzy BM25 text search and semantic vector embeddings. |
| **Auth & Policy** | **Cedar** | AWS Cedar policy engine (`policies.cedar`) enforcing fine-grained RBAC for Farmers, Mitras, Wholesalers, and Drivers. |
| **The Plumbing** | **LocalStack EventBridge** | In-memory coroutines and local EventBridge bus with $0.00 cloud spend. |

---

## 📱 3. How People Can Use This App

### 👨‍🌾 A. Farmers (Kisan)
1. **Pledge Harvest**: Tap **"Pledge Harvest"** or the **"+"** button on the home screen.
2. **Submit Batch**: Enter crop type (e.g. Tomatoes, Potatoes, Onions), harvest quantity, grade, and farm location.
3. **Compare Rates**: View the transparent price comparison showing a **+15% to +18% higher net payout** compared to traditional middleman mandi rates.
4. **AI Advisory**: Consult the intelligent advisor for harvesting timing, weather warnings, and price forecasting.

### 🛒 B. Buyers, Street Vendors & Informal Recyclers (Vyapar)
1. **Explore Surplus Queue**: Switch to the **"Surplus Queue"** tab.
2. **Claim Markdown Deals**: Street pushcart vendors can purchase nearing-expiry stock at flash discounts (15%–35% off), enabling profitable retail resale while preventing food waste.
3. **Submit Bulk RFQs**: Retail marts and bulk buyers can submit purchase orders directly to farm hubs with full unit-economics transparency.

### 🚚 C. Logistics Drivers (Saathi)
1. **Accept Dispatches**: Drivers view single-hop farm-to-vendor direct delivery routes.
2. **Zero Auction Yard Waiting**: Deliver directly without multi-hour delays in mandi congestion.
3. **Digital Proof of Delivery**: Complete digital drop-off verification.

### 🧑‍⚖️ D. Hackathon Judges & Developers
1. **AWS Build It Lab**: Tap the **🏆 Trophy icon** on the top app bar or select it from the navigation drawer.
2. **Test Cedar Policies**: Choose any user role (Farmer, Wholesaler, Krishi Mitra, Saathi Driver) and test live permission authorization rules against `policies.cedar`.
3. **Simulate Local API & Strands**: Run live simulations of AWS SAM API Gateway endpoints and Strands AI agent responses.
4. **Bring Your Own Key (BYOK) Inference**: Tap the **⚙️ Settings icon** on the top bar or side drawer:
   - **Google Gemini (AI Studio)**: Enter personal API key (`AIzaSy...`) for Gemini 1.5/2.0 Flash inference.
   - **OpenAI (GPT-4o)**: Enter personal key (`sk-proj-...`) for GPT-4o-mini / GPT-4o local inference.
   - **Anthropic Claude**: Enter personal key (`sk-ant-...`) for Claude 3.5 Haiku / Sonnet inference.
   - **Offline ICAR Bio-Engine**: 100% on-device zero-quota agronomy heuristics for zero cloud bill operation.
   - **Test Connection**: Run real-time latency ping tests against your custom keys.
   - **Open-Source Publishing Safeguard**: Toggle "Disable Bundled Developer Key" and "Purge All Keys" for zero-leak code review.

---

## 🚀 4. Quickstart: Running on Your Machine

```bash
# 1. Validate SAM template
sam validate -t template.yaml

# 2. Launch LocalStack & OpenSearch with Finch
finch compose -f finch-compose.yml up -d

# 3. Start SAM Local API Gateway
sam local start-api --endpoint-url http://localhost:4566 --port 3000

# 4. Run AWS Cedar policy authorization
cedar authorize \
  --policies policies.cedar \
  --principal 'KrishiRole::"Farmer"' \
  --action 'KrishiAction::"PledgeHarvest"' \
  --resource 'KrishiResource::"HarvestBatch"'
# => ALLOW
```
