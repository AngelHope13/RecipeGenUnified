// js/mealDetail.js

// Get the meal ID from the URL
const params = new URLSearchParams(window.location.search);
const mealId = params.get('id');

// Show loading state
document.getElementById('mealTitle').textContent = 'Loading...';

if (mealId) {
    fetch(`https://www.themealdb.com/api/json/v1/1/lookup.php?i=${mealId}`)
        .then(res => res.json())
        .then(data => displayMeal(data.meals?.[0]))
        .catch(err => {
            console.error('Error fetching meal:', err);
            document.getElementById('mealTitle').textContent = 'Meal not found.';
        });
} else {
    document.getElementById('mealTitle').textContent = 'No meal ID provided.';
}

function displayMeal(meal) {
    if (!meal) {
        document.getElementById('mealTitle').textContent = 'Meal not found.';
        return;
    }

    // Meal title and image
    document.getElementById('mealImage').src = meal.strMealThumb;
    document.getElementById('mealTitle').textContent = meal.strMeal;

    // Ingredients and measurements
    const ingredientsDiv = document.getElementById('ingredients');
    ingredientsDiv.innerHTML = '<h3>Ingredients</h3>';
    for (let i = 1; i <= 20; i++) {
        const ingredient = meal[`strIngredient${i}`];
        const measure = meal[`strMeasure${i}`];
        if (ingredient && ingredient.trim()) {
            const div = document.createElement('div');
            div.innerHTML = `<strong>${ingredient}</strong>: ${measure}`;
            ingredientsDiv.appendChild(div);
        }
    }

    // Cooking instructions
    document.getElementById('instructions').innerHTML = `
        <h3>Instructions</h3>
        <p>${meal.strInstructions}</p>
    `;

    // YouTube video section
    const videoSection = document.getElementById('videoSection');
    const videoFrame = document.getElementById('mealVideo');
    videoSection.innerHTML = ''; // clear in case reused

    if (meal.strYoutube) {
        const videoId = meal.strYoutube.split('v=')[1];
        const ampIndex = videoId.indexOf('&');
        const cleanVideoId = ampIndex > -1 ? videoId.substring(0, ampIndex) : videoId;

        const iframe = document.createElement('iframe');
        iframe.id = 'mealVideo';
        iframe.width = '100%';
        iframe.height = '400';
        iframe.src = `https://www.youtube.com/embed/${cleanVideoId}`;
        iframe.allowFullscreen = true;

        const link = document.createElement('a');
        link.href = meal.strYoutube;
        link.textContent = 'Watch on YouTube';
        link.target = '_blank';
        link.style.display = 'block';
        link.style.marginTop = '10px';

        videoSection.appendChild(iframe);
        videoSection.appendChild(link);
        videoSection.style.display = 'block';
    } else {
        videoSection.style.display = 'none';
    }
}
