package hu.aqyo8l.carshopping

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hu.aqyo8l.carshopping.db.CartManager
import hu.aqyo8l.carshopping.viewmodel.CarAdapter
import hu.aqyo8l.carshopping.viewmodel.CarViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: CarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val cartButton = findViewById<ImageButton>(R.id.cartButton)
        val cartBadge = findViewById<TextView>(R.id.cartBadge)

        val adapter = CarAdapter(emptyList(), true) {
            updateBadge(cartBadge)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel.cars.observe(this) {
            adapter.updateData(it)
        }

        updateBadge(cartBadge)

        cartButton.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val cartBadge = findViewById<TextView>(R.id.cartBadge)
        updateBadge(cartBadge)
    }

    private fun updateBadge(badge: TextView) {
        val count = CartManager.getCartItems().size
        badge.text = count.toString()
        badge.visibility = if (count > 0) View.VISIBLE else View.GONE
    }
}