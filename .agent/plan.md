# Project Plan

Update "GreenLoop" to focus on food waste reduction by generating recipes from expiring ingredients. This includes updating the data layer (Ingredients and RecipeTasks), the Dashboard UI (Inventory list), the Tasks UI (Recipe Generator with checkbox selection), and the Gemini API logic for structured JSON recipe generation.

## Project Brief

# Project Brief: GreenLoop

GreenLoop is a sustainability-focused Android application designed to combat food waste. By pivoting from general biowaste upcycling to intelligent kitchen management, the app helps users track their inventory and use Google Gemini AI to generate recipes from ingredients that are about to expire.

### Features

*   **Smart Fridge Inventory**: A "Fridge/Cupboard" dashboard that lists food items with "Expiring Soon" indicators. Users can quickly add items by scanning grocery receipts using **CameraX** and **Gemini AI**.
*   **AI Recipe Generator**: A dynamic tool in the Tasks tab where users select multiple ingredients via checkboxes and generate custom, waste-reducing recipes using the **Gemini SDK**.
*   **Guided Recipe Tasks**: Converts AI-generated recipes into structured, step-by-step interactive cards, complete with estimated preparation times and difficulty levels.
*   **Sustainability Impact Tracker**: A visual progress dashboard featuring a circular indicator for "CO2 Saved" and a history of rescued food items, gamifying the waste reduction process.

### High-Level Technical Stack

*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose with **Material Design 3** (supporting Full Edge-to-Edge display).
*   **Architecture**: MVVM (Model-View-ViewModel) with Navigation Compose.
*   **Asynchronous Logic**: Kotlin Coroutines and Flow.
*   **Generative AI**: **Google Gemini SDK** (Google AI Client) for receipt processing and JSON-structured recipe generation.
*   **Hardware Integration**: **CameraX** for high-performance receipt capturing.
*   **Networking & Serialization**: Retrofit with **Moshi** (utilizing **KSP** for efficient code generation).
*   **Dependency Management**: Version Catalog (`libs.versions.toml`) with **KSP (Kotlin Symbol Processing)** for all annotation-based processing.

## Implementation Steps
**Total Duration:** 42m 55s

### Task_1_Core_Foundation: Setup core data models, Room database for tracking waste and recipes, and integrate Gemini SDK for AI processing.
- **Status:** COMPLETED
- **Updates:** 1.  **Data Layer (Room Database)**:
- **Acceptance Criteria:**
  - Room database and DAOs are functional for Items, Recipes, and History
  - Gemini SDK is integrated with API_KEY configuration
  - Navigation architecture for Dashboard, Recipes, Progress, and Profile is implemented
  - Project builds successfully
- **Duration:** 17m 22s

### Task_2_Dashboard_Scanning: Implement the Dashboard UI with 'Items Expiring Soon' and CameraX integration for scanning receipts/waste.
- **Status:** COMPLETED
- **Updates:** 1.  **Intelligent Dashboard**:
- **Acceptance Criteria:**
  - Dashboard displays expiring items from the database
  - CameraX integration allows capturing images of waste or receipts
  - Gemini AI logic processes images to identify items and save them to Room
  - UI adheres to Material 3 design principles
- **Duration:** 5m 43s

### Task_3_Recipes_Progress_Profile: Develop the Recipe Library with upcycling guides, the Sustainability Tracker (CO2 savings), and User Profile settings.
- **Status:** COMPLETED
- **Updates:** 1.  **Upcycling Recipe Library**:
- **Acceptance Criteria:**
  - Recipe library shows categorized upcycling recipes with difficulty ratings
  - Circular CO2 progress tracker and historical log are functional
  - User Profile (living situation, allergies) filters/personalizes recipes
  - All screens are accessible via navigation
- **Duration:** 4m 35s

### Task_4_Refinement_Verification: Apply final Material 3 visual polish, create an adaptive icon, and perform final verification.
- **Status:** COMPLETED
- **Updates:** 1.  **Material 3 Visual Polish**:
- **Acceptance Criteria:**
  - Vibrant, energetic Material 3 color scheme and edge-to-edge display implemented
  - Adaptive app icon is present
  - Final build passes and app is stable (no crashes)
  - Verify application stability and alignment with user requirements; report any UI issues
- **Duration:** 5m 43s

### Task_5_Pivot_Data_And_Dashboard: Update Room entities for food ingredients and recipes. Refactor the Dashboard into a 'Fridge/Cupboard' inventory list with expiration indicators and update receipt scanning logic for food.
- **Status:** COMPLETED
- **Updates:** 1.  **Data Layer Migration**:
- **Acceptance Criteria:**
  - Room entities (Ingredient, Recipe) updated for food waste context
  - Dashboard UI displays 'Fridge/Cupboard' inventory with expiration badges
  - Gemini scanning logic updated to identify food items from receipts
  - Project builds successfully
- **Duration:** 6m 17s

### Task_6_AI_Recipe_Generator_And_Verify: Implement the AI Recipe Generator with checkbox ingredient selection, structured JSON recipe generation via Gemini, and interactive recipe cards. Perform final verification.
- **Status:** COMPLETED
- **Updates:** 1.  **AI Recipe Engine**:
    *   Updated `RecipeViewModel` to fetch current inventory from `IngredientRepository`.
    *   Implemented a checkbox selection system for users to choose specific ingredients they want to "rescue" (e.g., milk that's expiring tomorrow).
    *   Integrated **Gemini AI** with a structured prompt that strictly enforces JSON output. This ensures the app can reliably parse recipe names, prep times, and step-by-step instructions.
2.  **Interactive Tasks UI**:
    *   **Ingredient Selector**: A clean list of inventory items with checkboxes, allowing users to combine multiple ingredients into a single recipe.
    *   **Loading State**: Added a `CircularProgressIndicator` and animations to provide feedback while Gemini generates the recipe.
    *   **Interactive Recipe Cards**: Generated recipes are displayed in a dedicated card with numbered steps and a prominent "Rescue & Complete" action.
3.  **Sustainability Integration**:
    *   **Rescue Logic**: When a user completes an AI-generated recipe, the selected ingredients are automatically removed from the inventory.
    *   **CO2 Tracking**: Each rescued ingredient contributes an estimated **0.5kg of CO2 savings** to the user's sustainability progress.
4.  **Refinement & Stability**:
    *   Resolved build errors related to data model changes and navigation dependencies.
    *   Ensured the `NavGraph` correctly passes all required repositories to the `RecipeViewModel`.
    *   Verified that the project builds successfully and adheres to Material 3 design principles.
- **Acceptance Criteria:**
  - UI for selecting multiple ingredients via checkboxes is functional
  - Gemini integration generates recipes in structured JSON format
  - Interactive step-by-step recipe cards implemented
  - Sustainability tracker reflects food rescue history
  - Build pass, app does not crash, and verify application stability with critic_agent
- **Duration:** 3m 15s

