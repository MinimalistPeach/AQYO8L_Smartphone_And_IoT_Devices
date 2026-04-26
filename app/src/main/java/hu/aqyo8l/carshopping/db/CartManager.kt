package hu.aqyo8l.carshopping.db

import hu.aqyo8l.carshopping.model.Car

object CartManager {
    fun addToCart(car: Car) {
        DatabaseProvider.db().addToCart(car.id)
    }

    fun getCartItems(): List<Car> = DatabaseProvider.db().getCartItems()

    fun getCartCount(): Int = DatabaseProvider.db().getCartCount()

    fun removeOneFromCart(car: Car) {
        DatabaseProvider.db().removeOneFromCart(car.id)
    }

    fun clearCart() {
        DatabaseProvider.db().clearCart()
    }
}
