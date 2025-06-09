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

if (chatForm) {
    chatForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const query = userInput.value.trim();
        if (!query) return;

        appendMessage("user", query);
        userInput.value = "";

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
            appendMessage("bot", result.reply || "Here are some suggestions:");

            if (Array.isArray(result.recipes) && result.recipes.length > 0) {
                displayRecipes(result.recipes);
                showToast("Recipes loaded!");
            } else {
                displayRecipes([]);
                showToast("No recipes found.");
            }
        } catch (error) {
            console.error("❌ Chat error:", error);
            appendMessage("bot", "Sorry, something went wrong.");
            showToast("Something went wrong.");
        }
    });
}

function appendMessage(sender, text) {
    const msg = document.createElement("div");
    msg.className = sender === "user" ? "user-message" : "bot-message";
    msg.textContent = text;
    chatBox.appendChild(msg);
    chatBox.scrollTop = chatBox.scrollHeight;
}

// --- Clear Chat ---
function clearChat() {
    const chatBox = document.getElementById("chatBox");
    if (chatBox) {
        chatBox.innerHTML = '';
        appendMessage("bot", "Chat cleared. Start typing your ingredients again!");
        showToast("Chat cleared.");
    }
}

// --- Smart Suggestions Dropdown ---
const suggestionBox = document.createElement("div");
suggestionBox.id = "suggestionBox";
suggestionBox.style.display = "none";
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

// --- Load Nationalities Dropdown ---
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

// --- Toast Notification Setup ---
function createToastElement() {
    const toast = document.createElement("div");
    toast.id = "toast";
    toast.className = "toast";
    document.body.appendChild(toast);

    const style = document.createElement("style");
    style.textContent = `
        .toast {
            visibility: hidden;
            min-width: 250px;
            background-color: #1b5e20;
            color: #fff;
            text-align: center;
            border-radius: 12px;
            padding: 12px;
            position: fixed;
            z-index: 999;
            bottom: 30px;
            left: 50%;
            transform: translateX(-50%);
            font-size: 0.95em;
            box-shadow: 0 4px 8px rgba(0,0,0,0.2);
            transition: visibility 0s, opacity 0.3s ease-in-out;
            opacity: 0;
        }
        .toast.show {
            visibility: visible;
            opacity: 1;
        }
    `;
    document.head.appendChild(style);
}

function showToast(message = "Message sent!") {
    const toast = document.getElementById("toast");
    if (!toast) return;
    toast.textContent = message;
    toast.classList.add("show");
    setTimeout(() => toast.classList.remove("show"), 3000);
}

createToastElement();
