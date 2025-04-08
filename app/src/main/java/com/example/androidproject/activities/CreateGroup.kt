package com.example.androidproject.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.androidproject.R
import com.example.androidproject.R.layout.create_group
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.google.firebase.firestore.FieldValue

class CreateGroupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(create_group)

        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        btnSubmit.setOnClickListener {
            val groupName = findViewById<EditText>(R.id.etGroupName).text.toString()
            val groupDescription = findViewById<EditText>(R.id.etGroupDescription).text.toString()


            val groupData = hashMapOf(
                "groupName" to groupName,
                "description" to groupDescription,
                "createdAt" to FieldValue.serverTimestamp(),
                "createdBy" to "creatorUserId",
                "currency" to "EUR",
                "imageUrl" to "defaultImageUrl",
                "members" to arrayListOf<String>()
            )

            val db = FirebaseFirestore.getInstance()
            db.collection("groups")
                .add(groupData)
                .addOnSuccessListener { documentReference ->
                    Log.d(
                        "CreateGroupActivity",
                        "DocumentSnapshot added with ID: ${documentReference.id}"
                    )
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Log.w("CreateGroupActivity", "Error adding document", e)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
        }
    }
}

