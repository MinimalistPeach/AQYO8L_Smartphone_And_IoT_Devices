package hu.aqyo8l.carshopping.model

data class Car(
    val id: Int,
    val brand: String,
    val model: String,
    val price: Int,
    val imgUrl: String,
    val maxSpeedKmh: Int,
    val zeroToHundredSec: Double,
    val availableColors: List<String>
)
