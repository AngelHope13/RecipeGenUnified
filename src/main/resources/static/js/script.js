// --- LANGUAGE SWITCHER ---
function applyTranslations(lang) {
    if (!translations[lang]) return;
    const t = translations[lang];

    document.getElementById("greeting").innerText = t.greeting;
    document.getElementById("send-button").innerText = t.send;
    document.getElementById("nationality-label").innerText = t.chooseNationality;
    document.getElementById("vegetarian-label").innerText = t.vegetarian;
    document.getElementById("lowfat-label").innerText = t.lowFat;
    document.getElementById("under30-label").innerText = t.under30;

    if (document.getElementById("toast")?.innerText.includes("No recipes found")) {
        document.getElementById("toast").innerText = t.noMatch;
    }
}

const langSelect = document.getElementById("lang-switcher");
if (langSelect) {
    const savedLang = localStorage.getItem("lang") || "en";
    langSelect.value = savedLang;
    applyTranslations(savedLang);

    langSelect.addEventListener("change", () => {
        const selectedLang = langSelect.value;
        localStorage.setItem("lang", selectedLang);
        applyTranslations(selectedLang);
    });
}

// --- Recipe Card Generator ---
function displayRecipes(meals) {
    const resultContainer = document.getElementById("resultedRecipes");
    resultContainer.innerHTML = "";

    if (!meals || meals.length === 0) {
        resultContainer.innerHTML = `<p id="no-recipes-msg">${translations[langSelect.value]?.noMatch || "No recipes found. Try another ingredient!"}</p>`;
        return;
    }

    meals.forEach(meal => {
        const card = document.createElement("a");
        card.className = "result-card";
        card.href = `meal_detail.html?id=${meal.idMeal}`;
        card.innerHTML = `
            <img src="${meal.strMealThumb}" alt="${meal.strMeal}" />
            <h4>${meal.strMeal}</h4>
        `;
        resultContainer.appendChild(card);
    });
}

// --- Chatbot Interaction ---
const chatForm = document.getElementById("chatForm");
const chatBox = document.getElementById("chatBox");
const userInput = document.getElementById("userInput");
const suggestionBox = document.getElementById("suggestionBox");

function showLoader() {
    const loader = document.createElement("div");
    loader.id = "chatLoader";
    loader.className = "bot-message";
    loader.innerHTML = `<span class="spinner"></span> <em>Typing...</em>`;
    chatBox.appendChild(loader);
    chatBox.scrollTop = chatBox.scrollHeight;
}

function hideLoader() {
    const loader = document.getElementById("chatLoader");
    if (loader) loader.remove();
}

if (chatForm) {
    chatForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const query = userInput.value.trim();
        if (!query) return;

        appendMessage("user", query);
        userInput.value = "";
        suggestionBox.innerHTML = "";

        showLoader();

        const area = document.getElementById("countrySelect")?.value || "";
        const filters = {
            vegetarian: document.getElementById("vegetarianFilter")?.checked,
            lowFat: document.getElementById("lowFatFilter")?.checked,
            under30: document.getElementById("under30Filter")?.checked
        };

        try {
            const response = await fetch("http://localhost:8081/api/chat", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ message: query, area, filters })
            });
            const result = await response.json();

            hideLoader();
            appendMessage("bot", result.reply || "Here are some suggestions:");

            if (Array.isArray(result.recipes) && result.recipes.length > 0) {
                displayRecipes(result.recipes);
                showToast("Recipes loaded from Gemini!");
            } else {
                displayRecipes([]);
                showToast("No Gemini recipes found.");
            }

            const mealDbRes = await fetch(`https://www.themealdb.com/api/json/v1/1/filter.php?i=${encodeURIComponent(query)}`);
            const mealDbData = await mealDbRes.json();
            if (mealDbData.meals && mealDbData.meals.length > 0) {
                appendMessage("bot", "<em>Here are real recipes from TheMealDB:</em>");
                displayRecipes(mealDbData.meals);
                showToast("Recipes loaded from TheMealDB!");
            }

        } catch (error) {
            hideLoader();
            console.error("❌ Chat or API error:", error);
            appendMessage("bot", "Something went wrong while fetching recipes.");
            showToast("Error loading recipes.");
        }
    });
}

// --- Append Message to Chat ---
function appendMessage(sender, text) {
    const msg = document.createElement("div");
    msg.className = sender === "user" ? "user-message" : "bot-message";

    text = text.replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>");
    text = text.replace(/\n/g, "<br>");
    if (text.includes("•")) {
        text = text.split("•")
            .map(line => line.trim())
            .filter(Boolean)
            .map(item => `<li>${item}</li>`)
            .join("");
        text = `<ul>${text}</ul>`;
    }

    const emojiMap = {
        chicken: "🍗", beef: "🥩", pork: "🥓", egg: "🥚", garlic: "🧄", onion: "🧅",
        cheese: "🧀", carrot: "🥕", fish: "🐟", shrimp: "🦐", crab: "🦀", tomato: "🍅",
        potato: "🥔", rice: "🍚", pasta: "🍝", milk: "🥛", butter: "🧈", bread: "🍞",
        apple: "🍎", banana: "🍌", lemon: "🍋", orange: "🍊"
    };

    text = text.replace(/\b(\w+)\b/g, word =>
        emojiMap[word.toLowerCase()] ? `${emojiMap[word.toLowerCase()]} ${word}` : word
    );

    msg.innerHTML = text;
    chatBox.appendChild(msg);
    chatBox.scrollTop = chatBox.scrollHeight;
}

// --- Toast Notification ---
function createToastElement() {
    const toast = document.getElementById("toast");
    if (!toast) {
        const newToast = document.createElement("div");
        newToast.id = "toast";
        newToast.className = "toast";
        document.body.appendChild(newToast);
    }
}

function showToast(message = "Message sent!") {
    const toast = document.getElementById("toast");
    if (!toast) return;
    toast.textContent = message;
    toast.classList.add("show");
    setTimeout(() => toast.classList.remove("show"), 3000);
}
createToastElement();

// --- Load Country Nationalities ---
async function loadNationalities() {
    const select = document.getElementById("countrySelect");
    if (!select) return;

    try {
        const res = await fetch("https://www.themealdb.com/api/json/v1/1/list.php?a=list");
        const data = await res.json();
        data.meals.forEach(area => {
            const option = document.createElement("option");
            option.value = area.strArea;
            option.textContent = area.strArea;
            select.appendChild(option);
        });
    } catch (err) {
        console.error("Failed to load countries:", err);
    }
}
loadNationalities();

// --- Ingredient Autocomplete ---
let ingredientList = [];

async function fetchIngredients() {
    try {
        const res = await fetch("https://www.themealdb.com/api/json/v1/1/list.php?i=list");
        const data = await res.json();
        ingredientList = data.meals.map(meal => meal.strIngredient.toLowerCase());
    } catch (err) {
        console.error("Failed to fetch ingredients:", err);
    }
}
fetchIngredients();

userInput.addEventListener("input", () => {
    const query = userInput.value.toLowerCase().trim();
    if (!query || ingredientList.length === 0) {
        suggestionBox.innerHTML = "";
        suggestionBox.style.display = "none";
        return;
    }

    const matches = ingredientList
        .filter(ing => ing.includes(query))
        .slice(0, 5);

    if (matches.length > 0) {
        suggestionBox.innerHTML = matches.map(ing =>
            `<div class="suggestion-item">${ing}</div>`
        ).join("");
        suggestionBox.style.display = "block";
    } else {
        suggestionBox.innerHTML = "";
        suggestionBox.style.display = "none";
    }
});

suggestionBox.addEventListener("click", (e) => {
    if (e.target.classList.contains("suggestion-item")) {
        userInput.value = e.target.textContent;
        suggestionBox.innerHTML = "";
        suggestionBox.style.display = "none";
    }
});
