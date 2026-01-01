package com.example.pantrypal.data.model

data class Category(
    val id: Int,
    val name: String,
    val emoji: String,
    val color: String
) {
    companion object {
        val DEFAULT_CATEGORIES = listOf(
            Category(1, "Dairy", "🥛", "#2196F3"),
            Category(2, "Produce", "🥬", "#4CAF50"),
            Category(3, "Meat", "🍗", "#F44336"),
            Category(4, "Seafood", "🐟", "#00BCD4"),
            Category(5, "Bakery", "🍞", "#FF9800"),
            Category(6, "Frozen", "🧊", "#03A9F4"),
            Category(7, "Beverages", "🥤", "#9C27B0"),
            Category(8, "Snacks", "🍿", "#FFEB3B"),
            Category(9, "Condiments", "🧴", "#795548"),
            Category(10, "Canned Goods", "🥫", "#607D8B"),
            Category(11, "Grains", "🍚", "#FF5722"),
            Category(12, "Eggs", "🥚", "#FFC107"),
            Category(13, "Other", "📦", "#9E9E9E")
        )

        fun getCategoryNames(): List<String> {
            return DEFAULT_CATEGORIES.map { it.name }
        }

        fun getEmojiForCategory(categoryName: String): String {
            return DEFAULT_CATEGORIES.find { 
                it.name.equals(categoryName, ignoreCase = true) 
            }?.emoji ?: "📦"
        }
    }
}
