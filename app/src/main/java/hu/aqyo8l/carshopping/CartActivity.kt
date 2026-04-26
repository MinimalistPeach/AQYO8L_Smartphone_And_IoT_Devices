package hu.aqyo8l.carshopping

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hu.aqyo8l.carshopping.db.CartManager
import hu.aqyo8l.carshopping.db.DatabaseProvider
import hu.aqyo8l.carshopping.viewmodel.CarAdapter

class CartActivity : AppCompatActivity() {
    private lateinit var adapter: CarAdapter
    private lateinit var emptyCartText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        DatabaseProvider.init(applicationContext)

        val recyclerView = findViewById<RecyclerView>(R.id.cartRecyclerView)
        val backButton = findViewById<Button>(R.id.backButton)
        val clearCartButton = findViewById<Button>(R.id.clearCartButton)
        val checkoutButton = findViewById<Button>(R.id.checkoutButton)
        emptyCartText = findViewById(R.id.emptyCartText)
        adapter = CarAdapter(
            cars = emptyList(),
            showAddButton = false,
            showRemoveButton = true,
            onRemoveFromCart = { refreshCart() }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        clearCartButton.setOnClickListener {
            CartManager.clearCart()
            refreshCart()
            Toast.makeText(this, "Cart cleared.", Toast.LENGTH_SHORT).show()
        }

        checkoutButton.setOnClickListener {
            if (CartManager.getCartCount() == 0) {
                Toast.makeText(this, "Cart is empty.", Toast.LENGTH_SHORT).show()
            } else {
                CartManager.clearCart()
                refreshCart()
                Toast.makeText(this, "Payment successful. Order placed!", Toast.LENGTH_LONG).show()
            }
        }

        backButton.setOnClickListener {
            finish()
        }

        refreshCart()
    }

    override fun onResume() {
        super.onResume()
        refreshCart()
    }

    private fun refreshCart() {
        val items = CartManager.getCartItems()
        adapter.updateData(items)
        emptyCartText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }
}
