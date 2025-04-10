package com.example.androidproject.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.R
import com.example.androidproject.adapters.FriendsAdapter
import com.example.androidproject.adapters.GroupsAdapter
import com.example.androidproject.model.Group
import com.example.androidproject.model.User
import com.example.androidproject.utils.ProfileImageLoader
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainFragment : Fragment() {

    private lateinit var profilePic: ImageView
    private lateinit var addGroupButton: FloatingActionButton
    private lateinit var friendsTextView: TextView
    private lateinit var groupsTextView: TextView
    private lateinit var recyclerView: RecyclerView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val friendsList = mutableListOf<User>()
    private val groupsList = mutableListOf<Group>()

    private lateinit var friendsAdapter: FriendsAdapter
    private lateinit var groupsAdapter: GroupsAdapter

    private var currentTab = TAB_FRIENDS
    private var groupsListener: ListenerRegistration? = null
    private var usersListener: ListenerRegistration? = null
    private var isDataLoaded = false

    companion object {
        const val TAB_FRIENDS = 0
        const val TAB_GROUPS = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI elements
        profilePic = view.findViewById(R.id.ivProfilePic)
        addGroupButton = view.findViewById(R.id.fabAddGroup)
        friendsTextView = view.findViewById(R.id.friendsTextView)
        groupsTextView = view.findViewById(R.id.groupsTextView)
        recyclerView = view.findViewById(R.id.recyclerView)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize adapters
        friendsAdapter = FriendsAdapter(friendsList)
        groupsAdapter = GroupsAdapter(groupsList, auth.currentUser?.uid ?: "")

        // Set up click listeners for adapters
        friendsAdapter.setOnItemClickListener { user ->
            // Handle friend click (e.g., open chat or profile)
        }

        groupsAdapter.setOnItemClickListener { group ->
            val action = MainFragmentDirections.actionMainFragmentToGroupExpensesFragment(group.id)
            findNavController().navigate(action)
        }

        groupsAdapter.setOnJoinClickListener { group ->
            if (group.members.contains(auth.currentUser?.uid)) {
                leaveGroup(group)
            } else {
                joinGroup(group)
            }
        }

        // Set up click listeners
        profilePic.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_profileFragment)
        }

        addGroupButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_createGroupFragment)
        }

        // Set up tab click listeners
        friendsTextView.setOnClickListener { switchToFriendsTab() }
        groupsTextView.setOnClickListener { switchToGroupsTab() }

        // Load user profile picture
        loadUserProfilePicture()

        // Default to friends tab
        switchToFriendsTab()
    }

    private fun loadUserProfilePicture() {
        context?.let { ctx ->
            ProfileImageLoader.loadCurrentUserProfileImage(ctx, profilePic)
        }
    }

    override fun onResume() {
        super.onResume()

        // Reload profile picture in case it was updated
        loadUserProfilePicture()

        // Reset data loaded flag
        isDataLoaded = false

        // Always reload current tab data
        if (currentTab == TAB_FRIENDS) {
            // Force reload friends data
            friendsList.clear()
            friendsAdapter.notifyDataSetChanged()
            loadFriends()
        } else {
            // Force reload groups data
            groupsList.clear()
            groupsAdapter.notifyDataSetChanged()
            loadGroups()
        }
    }

    override fun onStart() {
        super.onStart()
        // Ensure data is loaded when fragment starts
        if (!isDataLoaded) {
            if (currentTab == TAB_FRIENDS) {
                loadFriends()
            } else {
                loadGroups()
            }
        }
    }

    private fun switchToFriendsTab() {
        // Update UI for friends tab
        friendsTextView.setBackgroundResource(R.drawable.tab_selected_background)
        friendsTextView.setTextColor(resources.getColor(R.color.black))
        groupsTextView.setBackgroundResource(android.R.color.transparent)
        groupsTextView.setTextColor(resources.getColor(android.R.color.darker_gray))

        // Set adapter
        recyclerView.adapter = friendsAdapter

        // Force clear and reload friends data
        friendsList.clear()
        friendsAdapter.notifyDataSetChanged()

        // Load friends data
        loadFriends()

        // Remove groups listener if active
        groupsListener?.remove()

        currentTab = TAB_FRIENDS
    }

    private fun switchToGroupsTab() {
        // Update UI for groups tab
        groupsTextView.setBackgroundResource(R.drawable.tab_selected_background)
        groupsTextView.setTextColor(resources.getColor(R.color.black))
        friendsTextView.setBackgroundResource(android.R.color.transparent)
        friendsTextView.setTextColor(resources.getColor(android.R.color.darker_gray))

        // Set adapter
        recyclerView.adapter = groupsAdapter

        // Force clear and reload groups data
        groupsList.clear()
        groupsAdapter.notifyDataSetChanged()

        // Load groups data
        loadGroups()

        // Remove users listener if active
        usersListener?.remove()

        currentTab = TAB_GROUPS
    }

    private fun loadFriends() {
        // Show some loading indicator if you have one

        // Remove previous listener if exists
        usersListener?.remove()

        // Set up real-time listener for users collection
        usersListener = db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    Toast.makeText(context, "Error loading friends: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Clear the list before adding new items
                    friendsList.clear()

                    for (document in snapshot.documents) {
                        val user = User(
                            Uid = document.getString("Uid") ?: document.id,
                            Username = document.getString("Username") ?: "",
                            Mail = document.getString("Mail") ?: "",
                            ImgUrl = document.getString("ImgUrl"),
                            Password = document.getString("Password")
                        )
                        // Don't add current user to friends list
                        if (user.Uid != auth.currentUser?.uid) {
                            friendsList.add(user)
                        }
                    }

                    // Ensure UI updates even if the list is empty
                    friendsAdapter.notifyDataSetChanged()

                    // Mark data as loaded
                    isDataLoaded = true

                    // Log for debugging
                    println("Loaded ${friendsList.size} friends")

                    // Force refresh the adapter
                    friendsAdapter.refresh()
                }
            }
    }

    private fun loadGroups() {
        // Remove previous listener if exists
        groupsListener?.remove()

        // Set up real-time listener for groups collection
        groupsListener = db.collection("groups")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Error loading groups: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Clear the list before adding new items
                    groupsList.clear()

                    for (document in snapshot.documents) {
                        val members = document.get("members") as? List<String> ?: listOf()

                        // Handle createdBy as String
                        val createdBy = when (val createdByValue = document.get("createdBy")) {
                            is String -> createdByValue
                            else -> ""
                        }

                        val group = Group(
                            id = document.id,
                            groupName = document.getString("groupName") ?: document.getString("name") ?: "",
                            description = document.getString("description") ?: "",
                            createdBy = createdBy,
                            createdAt = document.getLong("createdAt") ?: 0,
                            imageUrl = document.getString("imageUrl"),
                            currency = document.getString("currency") ?: "USD",
                            members = members
                        )
                        groupsList.add(group)
                    }

                    // Ensure UI updates even if the list is empty
                    groupsAdapter.notifyDataSetChanged()

                    // Mark data as loaded
                    isDataLoaded = true
                }
            }
    }

    private fun joinGroup(group: Group) {
        val userId = auth.currentUser?.uid ?: return

        // Check if user is already a member
        if (group.members.contains(userId)) {
            Toast.makeText(context, "You are already a member of this group", Toast.LENGTH_SHORT).show()
            return
        }

        // Add user to members list
        val updatedMembers = group.members.toMutableList()
        updatedMembers.add(userId)

        // Update group in Firestore
        db.collection("groups").document(group.id)
            .update("members", updatedMembers)
            .addOnSuccessListener {
                Toast.makeText(context, "Successfully joined group", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error joining group: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun leaveGroup(group: Group) {
        val userId = auth.currentUser?.uid ?: return

        // Check if user is a member
        if (!group.members.contains(userId)) {
            Toast.makeText(context, "You are not a member of this group", Toast.LENGTH_SHORT).show()
            return
        }

        // Remove user from members list
        val updatedMembers = group.members.toMutableList()
        updatedMembers.remove(userId)

        // Update group in Firestore
        db.collection("groups").document(group.id)
            .update("members", updatedMembers)
            .addOnSuccessListener {
                Toast.makeText(context, "Successfully left group", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error leaving group: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove listeners
        groupsListener?.remove()
        usersListener?.remove()
    }
}
