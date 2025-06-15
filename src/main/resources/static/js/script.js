// --- Generate Clickable Recipe Cards ---
function displayRecipes(meals) {
    const resultContainer = document.getElementById("resultedRecipes");
    resultContainer.innerHTML = "";

    if (!meals || meals.length === 0) {
        resultContainer.innerHTML = "<p>No recipes found. Try another ingredient!</p>";
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

// ✅ Clear Chat Function
function clearChat() {
    if (chatBox) {
        chatBox.innerHTML = "";
        appendMessage("bot", "Chat cleared. Start typing your ingredients again!");
        showToast("Chat cleared.");
    }
}

// ✅ Append Message with Formatting + Emoji
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

// ✅ Smart Suggestions
const suggestionBox = document.createElement("div");
suggestionBox.id = "suggestionBox";
suggestionBox.style.display = "none";
suggestionBox.style.position = "absolute";
suggestionBox.style.zIndex = "1000";
suggestionBox.style.backgroundColor = "#fff";
suggestionBox.style.border = "1px solid #ccc";
suggestionBox.style.borderRadius = "8px";
suggestionBox.style.boxShadow = "0 2px 8px rgba(0,0,0,0.1)";
suggestionBox.style.maxHeight = "200px";
suggestionBox.style.overflowY = "auto";
document.body.appendChild(suggestionBox);

let suggestions = [];
let selectedIndex = -1;

userInput.addEventListener("input", async () => {
    const lastWord = userInput.value.trim().split(" ").pop();
    if (!lastWord) return hideSuggestions();

    try {
        const res = await fetch(`http://localhost:8081/api/suggestions?input=${lastWord}`);
        suggestions = await res.json();
        selectedIndex = -1;
        showSuggestions(suggestions);
    } catch {
        hideSuggestions();
    }
});

userInput.addEventListener("keydown", (e) => {
    if (suggestionBox.style.display === "none") return;
    const items = suggestionBox.querySelectorAll("div");

    if (e.key === "ArrowDown") {
        selectedIndex = (selectedIndex + 1) % suggestions.length;
        updateSelection(items);
        e.preventDefault();
    } else if (e.key === "ArrowUp") {
        selectedIndex = (selectedIndex - 1 + suggestions.length) % suggestions.length;
        updateSelection(items);
        e.preventDefault();
    } else if (e.key === "Enter" && selectedIndex >= 0) {
        items[selectedIndex].click();
        e.preventDefault();
    }
});

function showSuggestions(list) {
    suggestionBox.innerHTML = "";
    list.forEach(item => {
        const div = document.createElement("div");
        div.textContent = item;
        div.style.cursor = "pointer";
        div.style.padding = "6px 10px";
        div.style.borderBottom = "1px solid #eee";
        div.addEventListener("click", () => {
            const words = userInput.value.trim().split(" ");
            words[words.length - 1] = item;
            userInput.value = words.join(" ") + " ";
            hideSuggestions();
            userInput.focus();
        });
        suggestionBox.appendChild(div);
    });

    const rect = userInput.getBoundingClientRect();
    suggestionBox.style.left = `${rect.left + window.scrollX}px`;
    suggestionBox.style.top = `${rect.bottom + window.scrollY}px`;
    suggestionBox.style.width = `${rect.width}px`;
    suggestionBox.style.display = list.length ? "block" : "none";
}

function updateSelection(items) {
    items.forEach((item, idx) => {
        item.style.backgroundColor = idx === selectedIndex ? "#f6c90e" : "#fff";
        item.style.color = idx === selectedIndex ? "#1b5e20" : "#000";
    });
}

function hideSuggestions() {
    suggestionBox.style.display = "none";
    suggestions = [];
    selectedIndex = -1;
}

document.addEventListener("click", (e) => {
    if (!suggestionBox.contains(e.target) && e.target !== userInput) {
        hideSuggestions();
    }
});

// ✅ Load nationalities from TheMealDB
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

// ✅ Toast Setup
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
