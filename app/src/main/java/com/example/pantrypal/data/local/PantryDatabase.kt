package com.example.pantrypal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pantrypal.data.model.PantryItem
import com.example.pantrypal.data.model.ShoppingItem
import com.example.pantrypal.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
@Database(
    entities = [
        PantryItem::class,
        ShoppingItem::class,
        User::class
    ],
    version = 2,
    exportSchema = false
)


abstract class PantryDatabase : RoomDatabase() {

    abstract fun pantryDao(): PantryDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: PantryDatabase? = null
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN profile_picture TEXT")
            }
        }
        fun getDatabase(context: Context): PantryDatabase {

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PantryDatabase::class.java,
                    "pantry_database"
                )

                    .addCallback(DatabaseCallback())
                    .addMigrations(MIGRATION_1_2)  // 👈 Add migration here
                    .fallbackToDestructiveMigration()  // Fallback if migration fails
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }


    // Callback to populate database with sample data (optional)
    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // You can add sample data here if needed
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateSampleData(database.pantryDao())
                }
            }
        }

        suspend fun populateSampleData(pantryDao: PantryDao) {
            // Add some sample items for testing
            val currentTime = System.currentTimeMillis()
            val dayInMillis = 24 * 60 * 60 * 1000L

            val sampleItems = listOf(
                PantryItem(
                    name = "Whole Milk",
                    category = "Dairy",
                    quantity = "1",
                    unit = "liter",
                    purchaseDate = currentTime - (3 * dayInMillis),
                    expiryDate = currentTime + (2 * dayInMillis)
                ),
                PantryItem(
                    name = "White Bread",
                    category = "Bakery",
                    quantity = "1",
                    unit = "loaf",
                    purchaseDate = currentTime - (2 * dayInMillis),
                    expiryDate = currentTime + (3 * dayInMillis)
                ),
                PantryItem(
                    name = "Fresh Lettuce",
                    category = "Produce",
                    quantity = "1",
                    unit = "head",
                    purchaseDate = currentTime - (1 * dayInMillis),
                    expiryDate = currentTime + (5 * dayInMillis)
                ),
                PantryItem(
                    name = "Eggs",
                    category = "Eggs",
                    quantity = "12",
                    unit = "pieces",
                    purchaseDate = currentTime - (5 * dayInMillis),
                    expiryDate = currentTime + (14 * dayInMillis)
                ),
                PantryItem(
                    name = "Chicken Breast",
                    category = "Meat",
                    quantity = "500",
                    unit = "grams",
                    purchaseDate = currentTime,
                    expiryDate = currentTime + (1 * dayInMillis)
                ),
                PantryItem(
                    name = "Cheddar Cheese",
                    category = "Dairy",
                    quantity = "200",
                    unit = "grams",
                    purchaseDate = currentTime - (7 * dayInMillis),
                    expiryDate = currentTime + (21 * dayInMillis)
                ),
                PantryItem(
                    name = "Greek Yogurt",
                    category = "Dairy",
                    quantity = "4",
                    unit = "cups",
                    purchaseDate = currentTime - (2 * dayInMillis),
                    expiryDate = currentTime + (7 * dayInMillis)
                ),
                PantryItem(
                    name = "Orange Juice",
                    category = "Beverages",
                    quantity = "1",
                    unit = "liter",
                    purchaseDate = currentTime - (4 * dayInMillis),
                    expiryDate = currentTime + (10 * dayInMillis)
                )
            )

            pantryDao.insertAllItems(sampleItems)
        }
    }
}
