package hu.aqyo8l.carshopping.viewmodel

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import hu.aqyo8l.carshopping.R
import hu.aqyo8l.carshopping.db.CartManager
import hu.aqyo8l.carshopping.model.Car
import java.util.Locale

class CarAdapter(
    private var cars: List<Car>,
    private val showAddButton: Boolean = true,
    private val showRemoveButton: Boolean = false,
    private val showManageButtons: Boolean = false,
    private val onAddToCart: (() -> Unit)? = null,
    private val onRemoveFromCart: (() -> Unit)? = null,
    private val onEditCar: ((Car) -> Unit)? = null,
    private val onDeleteCar: ((Car) -> Unit)? = null
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    class CarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val brand: TextView = view.findViewById(R.id.brandText)
        val model: TextView = view.findViewById(R.id.modelText)
        val price: TextView = view.findViewById(R.id.priceText)
        val maxSpeed: TextView = view.findViewById(R.id.maxSpeedText)
        val acceleration: TextView = view.findViewById(R.id.accelerationText)
        val colors: LinearLayout = view.findViewById(R.id.colorsContainer)
        val image: ImageView = view.findViewById(R.id.carImage)
        val addButton: Button = view.findViewById(R.id.addToCartButton)
        val removeButton: Button = view.findViewById(R.id.removeFromCartButton)
        val manageButtonsContainer: LinearLayout = view.findViewById(R.id.manageButtonsContainer)
        val editButton: Button = view.findViewById(R.id.editCarButton)
        val deleteButton: Button = view.findViewById(R.id.deleteCarButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]
        holder.brand.text = car.brand
        holder.model.text = car.model
        holder.price.text = "$${car.price}"
        holder.maxSpeed.text = "Max speed: ${car.maxSpeedKmh} km/h"
        holder.acceleration.text = String.format(
            Locale.US,
            "0-100 km/h: %.1f s",
            car.zeroToHundredSec
        )

        Glide.with(holder.itemView.context)
            .load(car.imgUrl)
            .into(holder.image)

        renderColorDots(holder, car)

        if (showAddButton) {
            holder.addButton.visibility = View.VISIBLE
            holder.addButton.setOnClickListener {
                CartManager.addToCart(car)
                onAddToCart?.invoke()
            }
        } else {
            holder.addButton.visibility = View.GONE
        }

        if (showRemoveButton) {
            holder.removeButton.visibility = View.VISIBLE
            holder.removeButton.setOnClickListener {
                CartManager.removeOneFromCart(car)
                onRemoveFromCart?.invoke()
            }
        } else {
            holder.removeButton.visibility = View.GONE
        }

        if (showManageButtons) {
            holder.manageButtonsContainer.visibility = View.VISIBLE
            holder.editButton.setOnClickListener {
                onEditCar?.invoke(car)
            }
            holder.deleteButton.setOnClickListener {
                onDeleteCar?.invoke(car)
            }
        } else {
            holder.manageButtonsContainer.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = cars.size

    fun updateData(newCars: List<Car>) {
        cars = newCars
        notifyDataSetChanged()
    }

    private fun renderColorDots(holder: CarViewHolder, car: Car) {
        holder.colors.removeAllViews()
        val density = holder.itemView.resources.displayMetrics.density
        val dotSizePx = (16 * density).toInt()
        val marginPx = (8 * density).toInt()

        car.availableColors.forEach { colorHex ->
            val dotView = View(holder.itemView.context)
            dotView.layoutParams = LinearLayout.LayoutParams(dotSizePx, dotSizePx).apply {
                marginEnd = marginPx
            }
            dotView.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(parseColorOrDefault(colorHex))
                setStroke((1 * density).toInt().coerceAtLeast(1), Color.DKGRAY)
            }
            dotView.contentDescription = "Color $colorHex"
            holder.colors.addView(dotView)
        }
    }

    private fun parseColorOrDefault(colorHex: String): Int {
        return try {
            Color.parseColor(colorHex)
        } catch (_: IllegalArgumentException) {
            Color.GRAY
        }
    }
}
