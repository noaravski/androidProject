package com.example.androidproject.activities

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.androidproject.R
import com.example.androidproject.model.CloudinaryModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.HashMap
import java.util.UUID
import kotlin.text.insert

class CreateGroupActivity : AppCompatActivity() {

    private lateinit var groupNameEditText: EditText
    private lateinit var groupDescriptionEditText: EditText
    private lateinit var uploadButton: de.hdodenhof.circleimageview.CircleImageView
    private lateinit var submitButton: Button

    private var selectedImageUri: Uri? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var cloudinaryModel: CloudinaryModel

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                uploadButton.setImageURI(selectedImageUri)
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_group)

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        groupNameEditText = findViewById(R.id.etGroupName)
        groupDescriptionEditText = findViewById(R.id.etGroupDescription)
        uploadButton = findViewById(R.id.group_image)
        submitButton = findViewById(R.id.btnSubmit)

        //Initialize Cloudinary
        cloudinaryModel = CloudinaryModel()

        // Set up click listeners


        uploadButton.setOnClickListener {
            val photoUri = createImageUri() // Create a URI for the image
            photoUri?.let {
                selectedImageUri = it
                takePicture.launch(it)
                uploadButton.setImageURI(selectedImageUri)
            }
        }
        submitButton.setOnClickListener { createGroup() }
    }

    private fun createGroup() {
        val groupName = groupNameEditText.text.toString().trim()
        val groupDescription = groupDescriptionEditText.text.toString().trim()

        if (groupName.isEmpty()) {
            groupNameEditText.error = "Group name is required"
            return
        }

        // Create group in Firestore
        val groupId = UUID.randomUUID().toString()
        val userId = auth.currentUser?.uid ?: return


        val group = HashMap<String, Any>()
        group["groupName"] = groupName
        group["description"] = groupDescription
        group["createdBy"] = userId
        group["createdAt"] = System.currentTimeMillis()
        group["currency"] = "EUR"
        group["imageUrl"] = "default"
        group["members"] = listOf(userId)

        if (selectedImageUri != null) {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
            cloudinaryModel.uploadImage(bitmap, "group_image_$groupId",
                onSuccess = { imageUrl ->
                    group["imageUrl"] = imageUrl ?: "default"
                    saveGroupToFirestore(groupId, group)
                },
                onError = { error ->
                    Toast.makeText(
                        this@CreateGroupActivity,
                        "Failed to upload image: $error",
                        Toast.LENGTH_SHORT
                    ).show()
                    saveGroupToFirestore(groupId, group)
                }
            )
        } else {
            saveGroupToFirestore(groupId, group)
        }
    }

    private fun createImageUri(): Uri? {
        val contentValues = ContentValues().apply {
            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "group_image_${System.currentTimeMillis()}.jpg"
            )
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    private fun saveGroupToFirestore(groupId: String, group: Map<String, Any>) {
        db.collection("groups").document(groupId).set(group).addOnSuccessListener {
            Toast.makeText(
                this@CreateGroupActivity, "Group created successfully", Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(this@CreateGroupActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }.addOnFailureListener { e ->
            Toast.makeText(
                this@CreateGroupActivity, "Error creating group: ${e.message}", Toast.LENGTH_SHORT
            ).show()
        }
    }
}
