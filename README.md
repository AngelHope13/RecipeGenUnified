# 🍽️ Recipe Wizard

**Smart Ingredient-Based Recipe Generator** powered by **Spring Boot**, **Gemini AI**, and multiple food APIs.

Created by: **Joaica Ponce** & **Angel Hope Feniza**

---

## 🌟 Features

- 🧠 **Smart Chat Input**: Type casually (e.g., “I have garlic and pasta”) and get matching recipes.
- 🤖 **AI Cooking Tips**: Contextual advice powered by Gemini AI — from ingredient hacks to food myths.
- 🧂 **Funny Fallbacks**: If the input is unknown or funny (e.g., “giraffe”), enjoy witty, playful replies.
- 🧪 **Chat Memory**: AI maintains awareness of previous messages for more meaningful follow-ups.
- 🥗 **Recipe Filters**: Choose from Vegetarian, Low Fat, or Under 30 Minutes.
- 🌎 **Cuisines**: Filter meals by nationality (American, Mexican, etc.)

---

## 🔗 API Integrations

1. 🌐 **Google Translate API** – Detects and translates languages dynamically
2. 🧠 **Gemini API** – AI-generated replies and cooking advice
3. 🍽️ **TheMealDB API** – Provides recipe suggestions based on ingredients
4. 🥄 **Spoonacular API** – Fallback recipe suggestions with richer metadata

---

## 🛠️ Tech Stack

- Java 17
- Spring Boot 3
- RestTemplate (HTTP Client)
- Gemini API via Google AI Studio
- Google Cloud Translate API
- HTML/CSS/JS frontend integration

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven or IDE (IntelliJ IDEA recommended)
- Valid API keys for:
    - Gemini
    - Google Translate
    - Spoonacular
    - TheMealDB (no key required)

### 1. Clone the Repo

```bash
git clone https://github.com/AngelHope13/RecipeGenUnified.git
cd RecipeGenUnified
