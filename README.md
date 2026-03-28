# 🌿 GreenLoop: The AI-Powered Biowaste Upcycling Assistant

**GreenLoop** turns your grocery waste into valuable resources. By combining OCR receipt scanning with a **Retrieval-Augmented Generation (RAG)** pipeline, we help users stop throwing away potential and start upcycling.

---

## 🚀 The Vision
Every year, billions of tons of food waste end up in landfills, contributing to 8% of global greenhouse gas emissions. **GreenLoop** gamifies the solution. Instead of manual data entry, we use AI to track your inventory and provide scientifically-backed upcycling "Tasks" (like making date-pit coffee or banana peel fertilizer) to ensure nothing goes to waste.

## 🛠️ Technical Architecture
We avoided building a "thin wrapper." Our backend uses a complex pipeline to ensure data accuracy and user safety.

| Layer | Technology | Role |
| :--- | :--- | :--- |
| **Mobile Frontend** | Android (Kotlin, Jetpack Compose) | Native performance & modern UI/UX |
| **AI Orchestration** | FastAPI (Python) | High-speed asynchronous logic |
| **Intelligence** | Gemini 2.5 Flash | Multimodal OCR & Contextual Synthesis |
| **Data Engine** | RAG (ChromaDB + Vector Embeddings) | Grounding AI in verified upcycling methods |
| **Architecture** | MVVM | Clean separation of concerns |



## ✨ Key Features
* **📸 Smart Receipt Scanner:** Snap a photo of your grocery receipt. Gemini Vision extracts items and estimates shelf-life automatically.
* **🧠 RAG-Powered Task Engine:** We don't just "guess." Our AI queries a curated database of upcycling methods to provide step-by-step guides.
* **📊 Impact Dashboard:** Track your progress with real-time metrics on estimated CO₂ and water saved.
* **♻️ Active Task Tracking:** Start long-term projects (like composting) and track your progress over days or weeks.

## 🏗️ Project Structure
* `app/`: Android Studio project (Kotlin/Compose).
* `server/`: FastAPI backend and RAG logic.
* `data/`: Curated JSON/Markdown files for the vector database.

## 🚦 Getting Started
1. **Clone the repository:** `git clone https://github.com/[YOUR-USERNAME]/GreenLoop.git`
2. **Backend Setup:**
   * Install requirements: `pip install -r requirements.txt`
   * Set your `GOOGLE_API_KEY` in `.env`.
   * Run: `uvicorn main:app --reload`
3. **Android Setup:**
   * Open the `app/` folder in Android Studio.
   * Sync Gradle (Kotlin DSL).
   * Run on an emulator or physical device (API 33+ recommended).

---

## 🔮 Future Roadmap
- [ ] **Smart Fridge Integration:** Syncing with IoT APIs for real-time inventory.
- [ ] **Community Marketplace:** Sharing upcycled products with neighbors.
- [ ] **Barcode Scanning:** For non-receipt items.

**Developed with ❤️ during the 2026 University of Bath Hackathon.**
