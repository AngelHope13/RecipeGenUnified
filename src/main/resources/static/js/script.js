// JavaScript for generating clickable recipe cards
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

// Hook to chatbot form submission
const chatForm = document.getElementById("chatForm");
if (chatForm) {
    chatForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const input = document.querySelector("#chatForm input[type='text']");
        const query = input.value.trim();
        if (!query) return;

        const chatBox = document.querySelector(".chat-box");
        const userMsg = document.createElement("div");
        userMsg.className = "message user";
        userMsg.textContent = query;
        chatBox.appendChild(userMsg);
        chatBox.scrollTop = chatBox.scrollHeight;

        input.value = "";

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
            console.log("✅ Received result from backend:", result);

            const botMsg = document.createElement("div");
            botMsg.className = "message bot";
            botMsg.textContent = result.reply || "Here are some suggestions:";
            chatBox.appendChild(botMsg);
            chatBox.scrollTop = chatBox.scrollHeight;

            if (Array.isArray(result.recipes) && result.recipes.length > 0) {
                displayRecipes(result.recipes);
            } else {
                console.warn("⚠️ No recipes found or recipe list is invalid:", result.recipes);
                displayRecipes([]);
            }
        } catch (error) {
            console.error("❌ Chat error:", error);
            const botMsg = document.createElement("div");
            botMsg.className = "message bot";
            botMsg.textContent = "Sorry, something went wrong.";
            chatBox.appendChild(botMsg);
        }
    });
}

// --- Smart Suggestions Dropdown and Enhanced Chat Logic ---
const mealGrid = document.getElementById("mealGrid");
const userInput = document.getElementById("userInput");
const chatBox = document.getElementById("chatBox");

const suggestionBox = document.createElement("div");
suggestionBox.id = "suggestionBox";
suggestionBox.style.display = "none";
document.body.appendChild(suggestionBox);

let suggestions = [];
let selectedIndex = -1;

userInput.addEventListener("input", async () => {
    const lastWord = userInput.value.trim().split(" ").pop();
    if (!lastWord) {
        hideSuggestions();
        return;
    }

    try {
        const res = await fetch(`http://localhost:8081/api/suggestions?input=${lastWord}`);
        suggestions = await res.json();
        selectedIndex = -1;
        showSuggestions(suggestions);
    } catch (err) {
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
    list.forEach((item, index) => {
        const div = document.createElement("div");
        div.textContent = item;
        div.style.cursor = "pointer";
        div.style.padding = "6px";
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

// --- Load Country Dropdown Dynamically ---
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
