// mealDetail.js

// 1. Extract Meal ID from URL
const params = new URLSearchParams(window.location.search);
const mealId = params.get("id");

// 2. Elements to update
const mealTitleEl = document.getElementById("mealTitle");
const mealImageEl = document.getElementById("mealImage");
const ingredientsEl = document.getElementById("ingredients");
const instructionsEl = document.getElementById("instructions");
const videoSectionEl = document.getElementById("videoSection");

// 3. Initial UI State
mealTitleEl.textContent = "Loading...";

// 4. Fetch and Display Meal
if (mealId) {
    fetch(`https://www.themealdb.com/api/json/v1/1/lookup.php?i=${mealId}`)
        .then(res => res.json())
        .then(data => displayMeal(data.meals?.[0]))
        .catch(err => {
            console.error("❌ Error fetching meal:", err);
            mealTitleEl.textContent = "⚠️ Meal not found.";
        });
} else {
    mealTitleEl.textContent = "⚠️ No meal ID provided.";
}

// 5. Display Logic
function displayMeal(meal) {
    if (!meal) {
        mealTitleEl.textContent = "⚠️ Meal not found.";
        return;
    }

    // Set meal title and image
    mealTitleEl.textContent = meal.strMeal;
    mealImageEl.src = meal.strMealThumb;
    mealImageEl.alt = meal.strMeal;

    // Render Ingredients
    ingredientsEl.innerHTML = "<h3>Ingredients</h3>";
    for (let i = 1; i <= 20; i++) {
        const ingredient = meal[`strIngredient${i}`];
        const measure = meal[`strMeasure${i}`];

        if (ingredient && ingredient.trim()) {
            const item = document.createElement("div");
            item.innerHTML = `<strong>${ingredient}</strong>: ${measure}`;
            ingredientsEl.appendChild(item);
        }
    }

    // Cooking Instructions
    instructionsEl.innerHTML = `
    <h3>Instructions</h3>
    <p>${meal.strInstructions}</p>
  `;

    // YouTube Video
    videoSectionEl.innerHTML = "";
    if (meal.strYoutube) {
        const videoId = extractYouTubeID(meal.strYoutube);
        const iframe = document.createElement("iframe");
        iframe.width = "100%";
        iframe.height = "400";
        iframe.src = `https://www.youtube.com/embed/${videoId}`;
        iframe.allowFullscreen = true;
        iframe.style.borderRadius = "12px";

        const link = document.createElement("a");
        link.href = meal.strYoutube;
        link.target = "_blank";
        link.textContent = "▶ Watch on YouTube";
        link.style.display = "block";
        link.style.marginTop = "10px";

        videoSectionEl.appendChild(iframe);
        videoSectionEl.appendChild(link);
        videoSectionEl.style.display = "block";
    } else {
        videoSectionEl.style.display = "none";
    }
}

// Utility: Extract YouTube Video ID
function extractYouTubeID(url) {
    const match = url.match(/[?&]v=([^&]+)/);
    return match ? match[1] : null;
}
