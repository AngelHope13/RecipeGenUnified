# 📡 API Documentation - RecipeGenUnified

## 🔗 Base URL

```
http://localhost:8081
```

---

## 🧠 POST `/smart-recipe/chat`

Processes natural language input or ingredient lists to provide smart cooking tips and recipe suggestions.

### 🔸 Request Body

```json
{
  "message": "I have chicken and garlic",
  "area": "American",
  "filters": {
    "vegetarian": false,
    "lowFat": false,
    "under30": false
  },
  "lang": "en"
}
```

### 🔸 Response

```json
{
  "reply": "<strong>Cooking Tip:</strong><br>Use garlic at the end for sharper flavor.<br><em>Want to know more about garlic's health benefits?</em>",
  "recipes": [
    {
      "strMeal": "Lemon Chicken",
      "strMealThumb": "https://...",
      "idMeal": "12345",
      "source": "spoonacular"
    }
  ]
}
```

---

## 🧾 GET `/recipes/{ingredient}`

Fetches recipes from TheMealDB based on a specific ingredient.

### 🔸 Path Parameter

- `ingredient`: *String* — e.g., `"chicken"`

### 🔸 Response

```json
[
  {
    "strMeal": "Chicken Alfredo",
    "strMealThumb": "https://...",
    "idMeal": "34567"
  }
]
```

---

## 🔍 GET `/ingredients/suggest?partial=broc`

Returns a list of suggested ingredient names matching a partial input.

### 🔸 Response

```json
["broccoli", "broccolini", "broccoli rabe"]
```

---

## 🌍 Internal: Translation API (Google Translate)

Used internally by the service layer.

- `translateToEnglish(String input)` → `String`
- `translateFromEnglish(String text, String targetLang)` → `String`

---

## 🧠 AI Integration

- Powered by **Gemini API** for smart, safety-filtered responses.
- Uses system prompts to avoid recipe duplication.
- Generates **cooking tips**, **myths**, and **ingredient hacks** only.

---

## 📦 Data Sources

- 🔌 **TheMealDB** - Ingredient & recipe database
- 🍲 **Spoonacular API** - Advanced recipe suggestions
- 🌐 **Google Translate API** - Auto language support
- 🤖 **Gemini API** - AI cooking tip generation

---

## 🛡 Filters Supported

- `vegetarian` — true/false
- `lowFat` — true/false
- `under30` — true/false (prep time estimation)

---

## 📌 Notes

- Results may include fallback tips if no clear match is found.
- System avoids mentioning full recipes or steps in AI tips.
- User input is language-detected and translated as needed.

---