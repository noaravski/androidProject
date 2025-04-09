package com.example.androidproject.views


import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.androidproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.HashMap
import java.util.UUID

class CreateGroupFragment : Fragment() {

    private lateinit var groupNameEditText: EditText
    private lateinit var groupDescriptionEditText: EditText
    private lateinit var uploadButton: ImageButton
    private lateinit var submitButton: Button

    private var selectedImageUri: Uri? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            uploadButton.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        groupNameEditText = view.findViewById(R.id.etGroupName)
        groupDescriptionEditText = view.findViewById(R.id.etGroupDescription)
        uploadButton = view.findViewById(R.id.btnUpload)
        submitButton = view.findViewById(R.id.btnSubmit)

        // Set up click listeners
        uploadButton.setOnClickListener { getContent.launch("image/*") }
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
        group["name"] = groupName
        group["description"] = groupDescription
        group["createdBy"] = userId
        group["createdAt"] = System.currentTimeMillis()

        if (selectedImageUri != null) {
            // Upload image to Firebase Storage
            val storageRef = storage.reference.child("group_images/$groupId")
            storageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        group["imageUrl"] = uri.toString()
                        saveGroupToFirestore(groupId, group)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    saveGroupToFirestore(groupId, group)
                }
        } else {
            saveGroupToFirestore(groupId, group)
        }
    }

    private fun saveGroupToFirestore(groupId: String, group: Map<String, Any>) {
        db.collection("groups").document(groupId)
            .set(group)
            .addOnSuccessListener {
                Toast.makeText(context, "Group created successfully", Toast.LENGTH_SHORT).show()
                // Navigate back to MainFragment
                findNavController().navigate(R.id.action_createGroupFragment_to_mainFragment)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error creating group: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
