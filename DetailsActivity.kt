package com.firstapp.FoodBridge

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.firstapp.FoodBridge.databinding.ActivityDetailsBinding
import com.firstapp.FoodBridge.model.CartItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private lateinit var auth: FirebaseAuth
    private var foodName: String? = null
    private var foodImage: String? = null
    private var foodDescriptions: String? = null
    private var foodIngredients: String? = null
    private var foodPrice: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase auth
        auth = FirebaseAuth.getInstance()

        foodName = intent.getStringExtra("MenuItemName")
        foodDescriptions = intent.getStringExtra("MenuItemDescription")
        foodIngredients = intent.getStringExtra("MenuItemIngredients")
        foodPrice = intent.getStringExtra("MenuItemPrice")
        foodImage = intent.getStringExtra("MenuItemImage")

//        val foodName = intent.getStringExtra("MenuItemName")
//        val foodImage = intent.getIntExtra("MenuItemImage",0)
//
//        binding.detailFoodName.text = foodName
//        binding.detailFoodImage.setImageResource(foodImage)
        with(binding) {
            detailFoodName.text = foodName
            detailDescription.text = foodDescriptions
            detailsIngredients.text = foodIngredients
            Glide.with(this@DetailsActivity).load(Uri.parse(foodImage)).into(detailFoodImage)
        }

        binding.imageButton.setOnClickListener {
            finish()
        }
        binding.addItemButtom.setOnClickListener {
            addItemToCart()
        }
    }

    private fun addItemToCart() {
        val database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid ?: ""

        // Create a cart items object
        val cartItem = CartItems(foodName.toString(),foodPrice.toString(),foodDescriptions.toString(),foodImage.toString(),1)

        // Save data to cart item to Firebase database
        database.child("user").child(userId).child("CartItems").push().setValue(cartItem).addOnSuccessListener {
            Toast.makeText(this,"Items added into cart successfully",Toast.LENGTH_SHORT).show()
        }.addOnFailureListener{
            Toast.makeText(this,"Item not added",Toast.LENGTH_SHORT).show()
        }
    }
}
