package hu.aqyo8l.carshopping.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hu.aqyo8l.carshopping.db.DatabaseProvider
import hu.aqyo8l.carshopping.model.Car

class CarViewModel : ViewModel() {
    private val _cars = MutableLiveData<List<Car>>()
    val cars: LiveData<List<Car>> = _cars

    fun loadCars() {
        _cars.value = DatabaseProvider.db().getCars()
    }
}
