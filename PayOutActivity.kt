package com.firstapp.hungersden

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firstapp.hungersden.databinding.ActivityPayOutBinding
import com.firstapp.hungersden.model.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PayOutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPayOutBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    private lateinit var name: String
    private lateinit var address: String
    private lateinit var phone: String
    private lateinit var totalAmount: String
    private lateinit var foodItemName: ArrayList<String>
    private lateinit var foodItemPrice: ArrayList<String>
    private lateinit var foodItemImage: ArrayList<String>
    private lateinit var foodItemDescription: ArrayList<String>
    private lateinit var foodItemIngredient: ArrayList<String>
    private lateinit var foodItemQuantities: ArrayList<Int>
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayOutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        setUserData()

        val intent = intent
        foodItemName = intent.getStringArrayListExtra("FoodItemName") ?: arrayListOf()
        foodItemPrice = intent.getStringArrayListExtra("FoodItemPrice") ?: arrayListOf()
        foodItemImage = intent.getStringArrayListExtra("FoodItemImage") ?: arrayListOf()
        foodItemDescription = intent.getStringArrayListExtra("FoodItemDescription") ?: arrayListOf()
        foodItemIngredient = intent.getStringArrayListExtra("FoodItemIngredient") ?: arrayListOf()
        foodItemQuantities = intent.getIntegerArrayListExtra("FoodItemQuantities") ?: arrayListOf()

        totalAmount = "${calculateTotalAmount()}₹"
        binding.totalAmount.setText(totalAmount)

        binding.placeMyOrderButton.setOnClickListener {
            name = binding.name.text.toString().trim()
            address = binding.address.text.toString().trim()
            phone = binding.phone.text.toString().trim()

            if (name.isBlank() || address.isBlank() || phone.isBlank()) {
                Toast.makeText(this, "Please enter all the details", Toast.LENGTH_SHORT).show()
            } else {
                placeOrder()
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        // Show toast when "Donate" button is clicked
        binding.btnDonate.setOnClickListener {
            Toast.makeText(this, "Thank you for your Donation !", Toast.LENGTH_SHORT).show()
        }
    }

    private fun placeOrder() {
        userId = auth.currentUser?.uid ?: return
        val time = System.currentTimeMillis()
        val itemPushKey = databaseReference.child("OrderDetails").push().key ?: return

        val orderDetails = OrderDetails(
            userId,
            name,
            foodItemName,
            foodItemPrice,
            foodItemImage,
            foodItemQuantities,
            address,
            totalAmount,
            phone,
            time,
            itemPushKey,
            false,
            false
        )

        val orderReference = databaseReference.child("OrderDetails").child(itemPushKey)
        orderReference.setValue(orderDetails).addOnSuccessListener {
            CongratsBottomSheet().show(supportFragmentManager, "Test")
            removeItemFromCart()
            addOrderToHistory(orderDetails)
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to place order", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addOrderToHistory(orderDetails: OrderDetails) {
        databaseReference.child("user").child(userId).child("BuyHistory")
            .child(orderDetails.itemPushKey!!).setValue(orderDetails)
    }

    private fun removeItemFromCart() {
        val cartItemReference = databaseReference.child("user").child(userId).child("CartItems")
        cartItemReference.removeValue()
    }

    private fun calculateTotalAmount(): Int {
        var total = 0
        for (i in foodItemPrice.indices) {
            val price = foodItemPrice[i].replace("₹", "").toIntOrNull() ?: 0
            val quantity = foodItemQuantities.getOrNull(i) ?: 1
            total += price * quantity
        }
        return total
    }

    private fun setUserData() {
        val user = auth.currentUser ?: return
        userId = user.uid
        val userReference = databaseReference.child("user").child(userId)

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.name.setText(snapshot.child("name").getValue(String::class.java) ?: "")
                    binding.address.setText(snapshot.child("address").getValue(String::class.java) ?: "")
                    binding.phone.setText(snapshot.child("phone").getValue(String::class.java) ?: "")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PayOutActivity, "Error loading user data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

