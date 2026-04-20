package hu.aqyo8l.carshopping.db

import hu.aqyo8l.carshopping.model.Car

object CartManager {
    private val cartItems = mutableListOf<Car>()

    fun addToCart(car: Car) {
        cartItems.add(car)
    }

    fun getCartItems(): List<Car> = cartItems
}
