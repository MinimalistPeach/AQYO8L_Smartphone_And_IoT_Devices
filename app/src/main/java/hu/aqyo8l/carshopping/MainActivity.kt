package hu.aqyo8l.carshopping

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hu.aqyo8l.carshopping.db.CartManager
import hu.aqyo8l.carshopping.db.DatabaseProvider
import hu.aqyo8l.carshopping.model.Car
import hu.aqyo8l.carshopping.viewmodel.CarAdapter
import hu.aqyo8l.carshopping.viewmodel.CarViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: CarViewModel by viewModels()
    private lateinit var adapter: CarAdapter
    private lateinit var cartButton: ImageButton
    private lateinit var cartBadge: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DatabaseProvider.init(applicationContext)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        cartButton = findViewById(R.id.cartButton)
        val addCarButton = findViewById<ImageView>(R.id.addCarButton)
        cartBadge = findViewById(R.id.cartBadge)
        val cartContainer = findViewById<View>(R.id.cartContainer)

        adapter = CarAdapter(
            cars = emptyList(),
            showAddButton = true,
            showManageButtons = true,
            onAddToCart = {
                updateBadge(cartBadge)
                playCartAddAnimation()
            },
            onEditCar = { showCarFormDialog(it) },
            onDeleteCar = { deleteCar(it) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel.cars.observe(this) {
            adapter.updateData(it)
        }
        viewModel.loadCars()

        cartContainer.bringToFront()

        updateBadge(cartBadge)

        cartButton.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
        addCarButton.setOnClickListener {
            showCarFormDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        val cartBadge = findViewById<TextView>(R.id.cartBadge)
        updateBadge(cartBadge)
    }

    private fun updateBadge(badge: TextView) {
        val count = CartManager.getCartCount()
        badge.text = count.toString()
        badge.visibility = if (count > 0) View.VISIBLE else View.GONE
    }

    private fun playCartAddAnimation() {
        val cartScaleX = ObjectAnimator.ofFloat(cartButton, View.SCALE_X, 1f, 1.14f, 1f)
        val cartScaleY = ObjectAnimator.ofFloat(cartButton, View.SCALE_Y, 1f, 1.14f, 1f)
        val badgeScaleX = ObjectAnimator.ofFloat(cartBadge, View.SCALE_X, 1f, 1.2f, 1f)
        val badgeScaleY = ObjectAnimator.ofFloat(cartBadge, View.SCALE_Y, 1f, 1.2f, 1f)

        AnimatorSet().apply {
            duration = 260
            interpolator = OvershootInterpolator(1.5f)
            playTogether(cartScaleX, cartScaleY, badgeScaleX, badgeScaleY)
            start()
        }
    }

    private fun showCarFormDialog(car: Car? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_car_form, null)
        val brandInput = dialogView.findViewById<EditText>(R.id.brandInput)
        val modelInput = dialogView.findViewById<EditText>(R.id.modelInput)
        val priceInput = dialogView.findViewById<EditText>(R.id.priceInput)
        val maxSpeedInput = dialogView.findViewById<EditText>(R.id.maxSpeedInput)
        val accelerationInput = dialogView.findViewById<EditText>(R.id.accelerationInput)
        val colorsInput = dialogView.findViewById<EditText>(R.id.colorsInput)
        val imageUrlInput = dialogView.findViewById<EditText>(R.id.imageUrlInput)

        if (car != null) {
            brandInput.setText(car.brand)
            modelInput.setText(car.model)
            priceInput.setText(car.price.toString())
            maxSpeedInput.setText(car.maxSpeedKmh.toString())
            accelerationInput.setText(car.zeroToHundredSec.toString())
            colorsInput.setText(car.availableColors.joinToString(","))
            imageUrlInput.setText(car.imgUrl)
        }

        AlertDialog.Builder(this)
            .setTitle(if (car == null) "Add Car" else "Edit Car")
            .setView(dialogView)
            .setPositiveButton(if (car == null) "Add" else "Save", null)
            .setNegativeButton("Cancel", null)
            .create()
            .also { dialog ->
                dialog.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val resultCar = buildCarFromForm(
                        existingCar = car,
                        brandInput = brandInput,
                        modelInput = modelInput,
                        priceInput = priceInput,
                        maxSpeedInput = maxSpeedInput,
                        accelerationInput = accelerationInput,
                        colorsInput = colorsInput,
                        imageUrlInput = imageUrlInput
                    ) ?: return@setOnClickListener

                    if (car == null) {
                        DatabaseProvider.db().addCar(resultCar)
                        Toast.makeText(this, "Car added.", Toast.LENGTH_SHORT).show()
                    } else {
                        DatabaseProvider.db().updateCar(resultCar)
                        Toast.makeText(this, "Car updated.", Toast.LENGTH_SHORT).show()
                    }
                    viewModel.loadCars()
                    dialog.dismiss()
                }
            }
    }

    private fun deleteCar(car: Car) {
        AlertDialog.Builder(this)
            .setTitle("Delete Car")
            .setMessage("Delete ${car.brand} ${car.model} from sale?")
            .setPositiveButton("Delete") { _, _ ->
                DatabaseProvider.db().deleteCar(car.id)
                viewModel.loadCars()
                updateBadge(cartBadge)
                Toast.makeText(this, "Car deleted.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun buildCarFromForm(
        existingCar: Car?,
        brandInput: EditText,
        modelInput: EditText,
        priceInput: EditText,
        maxSpeedInput: EditText,
        accelerationInput: EditText,
        colorsInput: EditText,
        imageUrlInput: EditText
    ): Car? {
        val brand = brandInput.text.toString().trim()
        val model = modelInput.text.toString().trim()
        val price = priceInput.text.toString().trim().toIntOrNull()
        val maxSpeed = maxSpeedInput.text.toString().trim().toIntOrNull()
        val acceleration = accelerationInput.text.toString().trim().toDoubleOrNull()
        val imageUrl = imageUrlInput.text.toString().trim()
        val colors = colorsInput.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (
            brand.isBlank() || model.isBlank() || price == null || maxSpeed == null ||
            acceleration == null || imageUrl.isBlank() || colors.isEmpty()
        ) {
            Toast.makeText(this, "Please fill all fields correctly.", Toast.LENGTH_SHORT).show()
            return null
        }

        return Car(
            id = existingCar?.id ?: DatabaseProvider.db().getNextCarId(),
            brand = brand,
            model = model,
            price = price,
            imgUrl = imageUrl,
            maxSpeedKmh = maxSpeed,
            zeroToHundredSec = acceleration,
            availableColors = colors
        )
    }
}
