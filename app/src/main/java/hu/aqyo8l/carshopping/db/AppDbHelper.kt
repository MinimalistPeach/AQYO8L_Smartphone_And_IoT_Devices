package hu.aqyo8l.carshopping.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import hu.aqyo8l.carshopping.model.Car
import androidx.core.database.sqlite.transaction

class AppDbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE cars (
                id INTEGER PRIMARY KEY,
                brand TEXT NOT NULL,
                model TEXT NOT NULL,
                price INTEGER NOT NULL,
                img_url TEXT NOT NULL,
                max_speed_kmh INTEGER NOT NULL,
                zero_to_hundred_sec REAL NOT NULL,
                available_colors TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE cart_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                car_id INTEGER NOT NULL,
                FOREIGN KEY(car_id) REFERENCES cars(id)
            )
            """.trimIndent()
        )

        seedCars(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS cart_items")
        db.execSQL("DROP TABLE IF EXISTS cars")
        onCreate(db)
    }

    fun getCars(): List<Car> {
        val cars = mutableListOf<Car>()
        readableDatabase.query(
            "cars",
            arrayOf(
                "id",
                "brand",
                "model",
                "price",
                "img_url",
                "max_speed_kmh",
                "zero_to_hundred_sec",
                "available_colors"
            ),
            null,
            null,
            null,
            null,
            "id ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                cars.add(
                    Car(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        brand = cursor.getString(cursor.getColumnIndexOrThrow("brand")),
                        model = cursor.getString(cursor.getColumnIndexOrThrow("model")),
                        price = cursor.getInt(cursor.getColumnIndexOrThrow("price")),
                        imgUrl = cursor.getString(cursor.getColumnIndexOrThrow("img_url")),
                        maxSpeedKmh = cursor.getInt(cursor.getColumnIndexOrThrow("max_speed_kmh")),
                        zeroToHundredSec = cursor.getDouble(
                            cursor.getColumnIndexOrThrow("zero_to_hundred_sec")
                        ),
                        availableColors = decodeColors(
                            cursor.getString(cursor.getColumnIndexOrThrow("available_colors"))
                        )
                    )
                )
            }
        }
        return cars
    }

    fun addToCart(carId: Int) {
        val values = ContentValues().apply {
            put("car_id", carId)
        }
        writableDatabase.insert("cart_items", null, values)
    }

    fun addCar(car: Car) {
        val values = ContentValues().apply {
            put("id", car.id)
            put("brand", car.brand)
            put("model", car.model)
            put("price", car.price)
            put("img_url", car.imgUrl)
            put("max_speed_kmh", car.maxSpeedKmh)
            put("zero_to_hundred_sec", car.zeroToHundredSec)
            put("available_colors", encodeColors(car.availableColors))
        }
        writableDatabase.insert("cars", null, values)
    }

    fun updateCar(car: Car) {
        val values = ContentValues().apply {
            put("brand", car.brand)
            put("model", car.model)
            put("price", car.price)
            put("img_url", car.imgUrl)
            put("max_speed_kmh", car.maxSpeedKmh)
            put("zero_to_hundred_sec", car.zeroToHundredSec)
            put("available_colors", encodeColors(car.availableColors))
        }
        writableDatabase.update("cars", values, "id = ?", arrayOf(car.id.toString()))
    }

    fun deleteCar(carId: Int) {
        writableDatabase.transaction {
            try {
                delete("cart_items", "car_id = ?", arrayOf(carId.toString()))
                delete("cars", "id = ?", arrayOf(carId.toString()))
            } finally {
            }
        }
    }

    fun getNextCarId(): Int {
        readableDatabase.rawQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM cars", null).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 1
        }
    }

    fun getCartItems(): List<Car> {
        val cartItems = mutableListOf<Car>()
        readableDatabase.rawQuery(
            """
            SELECT c.id, c.brand, c.model, c.price, c.img_url, c.max_speed_kmh,
                   c.zero_to_hundred_sec, c.available_colors
            FROM cart_items ci
            INNER JOIN cars c ON c.id = ci.car_id
            ORDER BY ci.id ASC
            """.trimIndent(),
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                cartItems.add(
                    Car(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        brand = cursor.getString(cursor.getColumnIndexOrThrow("brand")),
                        model = cursor.getString(cursor.getColumnIndexOrThrow("model")),
                        price = cursor.getInt(cursor.getColumnIndexOrThrow("price")),
                        imgUrl = cursor.getString(cursor.getColumnIndexOrThrow("img_url")),
                        maxSpeedKmh = cursor.getInt(cursor.getColumnIndexOrThrow("max_speed_kmh")),
                        zeroToHundredSec = cursor.getDouble(
                            cursor.getColumnIndexOrThrow("zero_to_hundred_sec")
                        ),
                        availableColors = decodeColors(
                            cursor.getString(cursor.getColumnIndexOrThrow("available_colors"))
                        )
                    )
                )
            }
        }
        return cartItems
    }

    fun getCartCount(): Int {
        readableDatabase.rawQuery("SELECT COUNT(*) FROM cart_items", null).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    fun removeOneFromCart(carId: Int) {
        writableDatabase.execSQL(
            """
            DELETE FROM cart_items
            WHERE id = (
                SELECT id FROM cart_items
                WHERE car_id = ?
                ORDER BY id DESC
                LIMIT 1
            )
            """.trimIndent(),
            arrayOf(carId)
        )
    }

    fun clearCart() {
        writableDatabase.delete("cart_items", null, null)
    }

    private fun seedCars(db: SQLiteDatabase) {
        val sampleCars = listOf(
            Car(1, "Toyota", "Corolla", 20000, SAMPLE_IMAGE_URL, 210, 8.2, listOf("#FFFFFF", "#111111", "#B0B0B0")),
            Car(2, "BMW", "M3", 70000, SAMPLE_IMAGE_URL, 250, 3.9, listOf("#0A3D91", "#111111", "#C62828")),
            Car(3, "Audi", "A4", 45000, SAMPLE_IMAGE_URL, 241, 5.8, listOf("#FAFAFA", "#7E8A97", "#1F1F1F")),
            Car(4, "Tesla", "Model 3", 50000, SAMPLE_IMAGE_URL, 225, 4.4, listOf("#F5F5F5", "#151515", "#1E88E5"))
        )

        sampleCars.forEach { car ->
            val values = ContentValues().apply {
                put("id", car.id)
                put("brand", car.brand)
                put("model", car.model)
                put("price", car.price)
                put("img_url", car.imgUrl)
                put("max_speed_kmh", car.maxSpeedKmh)
                put("zero_to_hundred_sec", car.zeroToHundredSec)
                put("available_colors", encodeColors(car.availableColors))
            }
            db.insert("cars", null, values)
        }
    }

    private fun encodeColors(colors: List<String>): String = colors.joinToString(",")

    private fun decodeColors(colors: String): List<String> {
        if (colors.isBlank()) {
            return emptyList()
        }
        return colors.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    companion object {
        private const val DB_NAME = "car_shopping.db"
        private const val DB_VERSION = 3
        private const val SAMPLE_IMAGE_URL =
            "https://upload.wikimedia.org/wikipedia/commons/1/12/1925_Ford_Model_T_touring.jpg"
    }
}
