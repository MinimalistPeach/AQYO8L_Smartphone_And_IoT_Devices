package hu.aqyo8l.carshopping.viewmodel

import hu.aqyo8l.carshopping.model.Car
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import hu.aqyo8l.carshopping.R
import hu.aqyo8l.carshopping.db.CartManager

class CarAdapter(private var cars: List<Car>,
                 private val showAddButton: Boolean = true,
                 private val onAddToCart: (() -> Unit)? = null) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    class CarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val brand: TextView = view.findViewById(R.id.brandText)
        val model: TextView = view.findViewById(R.id.modelText)
        val price: TextView = view.findViewById(R.id.priceText)
        val image: ImageView = view.findViewById(R.id.carImage)
        val button: Button = view.findViewById(R.id.addToCartButton)
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

        Glide.with(holder.itemView.context)
            .load(car.imgUrl)
            .into(holder.image)

        if (showAddButton) {
            holder.button.visibility = View.VISIBLE
            holder.button.setOnClickListener {
                CartManager.addToCart(car)
                onAddToCart?.invoke()
            }
        } else {
            holder.button.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = cars.size

    fun updateData(newCars: List<Car>) {
        cars = newCars
        notifyDataSetChanged()
    }
}