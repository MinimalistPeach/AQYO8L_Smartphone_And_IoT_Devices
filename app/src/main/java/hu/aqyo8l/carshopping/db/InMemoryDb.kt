package hu.aqyo8l.carshopping.db

import hu.aqyo8l.carshopping.model.Car

object InMemoryDb {
    private val cars = mutableListOf(
        Car(1, "Toyota", "Corolla", 20000, "https://upload.wikimedia.org/wikipedia/commons/1/12/1925_Ford_Model_T_touring.jpg"),
        Car(2, "BMW", "M3", 70000, "https://upload.wikimedia.org/wikipedia/commons/1/12/1925_Ford_Model_T_touring.jpg"),
        Car(3, "Audi", "A4", 45000, "https://upload.wikimedia.org/wikipedia/commons/1/12/1925_Ford_Model_T_touring.jpg"),
        Car(4, "Tesla", "Model 3", 50000, "https://upload.wikimedia.org/wikipedia/commons/1/12/1925_Ford_Model_T_touring.jpg")
    )

    fun getCars(): List<Car> = cars
}