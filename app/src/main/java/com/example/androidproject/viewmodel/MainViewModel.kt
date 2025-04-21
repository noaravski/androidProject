package com.example.androidproject.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.androidproject.data.repository.GroupRepository
import com.example.androidproject.model.Group
import com.example.androidproject.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val groupRepository = GroupRepository(application)
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // LiveData for groups
    private val _groups = MutableLiveData<List<Group>>()
    val groups: LiveData<List<Group>> = _groups

    // LiveData for friends (users)
    private val _friends = MutableLiveData<List<User>>()
    val friends: LiveData<List<User>> = _friends

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Load all groups
    fun loadGroups() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val groupsLiveData = groupRepository.getAllGroups()
                groupsLiveData.observeForever { groupsList ->
                    _groups.value = groupsList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading groups: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // Load all friends (users)
    fun loadFriends() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val snapshot = db.collection("users").get().await()
                val currentUserId = auth.currentUser?.uid

                val usersList = snapshot.documents.mapNotNull { document ->
                    val userId = document.getString("Uid") ?: document.id

                    // Skip current user
                    if (userId == currentUserId) return@mapNotNull null

                    User(
                        Uid = userId,
                        Username = document.getString("Username") ?: "",
                        Mail = document.getString("Mail") ?: "",
                        ImgUrl = document.getString("ImgUrl"),
                        Password = document.getString("Password")
                    )
                }

                _friends.value = usersList
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error loading friends: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // Join a group
    fun joinGroup(group: Group, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onError("User not logged in")

        if (group.members.contains(userId)) {
            onError("Already a member of this group")
            return
        }

        val updatedMembers = group.members.toMutableList().apply { add(userId) }

        viewModelScope.launch {
            try {
                val success = groupRepository.updateGroupMembers(group.id, updatedMembers)
                if (success) {
                    onSuccess()
                } else {
                    onError("Failed to join group")
                }
            } catch (e: Exception) {
                onError("Error joining group: ${e.message}")
            }
        }
    }

    // Leave a group
    fun leaveGroup(group: Group, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onError("User not logged in")

        if (!group.members.contains(userId)) {
            onError("Not a member of this group")
            return
        }

        val updatedMembers = group.members.toMutableList().apply { remove(userId) }

        viewModelScope.launch {
            try {
                val success = groupRepository.updateGroupMembers(group.id, updatedMembers)
                if (success) {
                    onSuccess()
                } else {
                    onError("Failed to leave group")
                }
            } catch (e: Exception) {
                onError("Error leaving group: ${e.message}")
            }
        }
    }

    // Create a new group
    fun createGroup(group: Group, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val success = groupRepository.createGroup(group)
                if (success) {
                    onSuccess()
                } else {
                    onError("Failed to create group")
                }
            } catch (e: Exception) {
                onError("Error creating group: ${e.message}")
            }
        }
    }
}
