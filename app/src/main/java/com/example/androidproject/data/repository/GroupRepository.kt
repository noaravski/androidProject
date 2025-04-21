package com.example.androidproject.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.example.androidproject.data.room.AppDatabase
import com.example.androidproject.data.room.GroupEntity
import com.example.androidproject.model.Group
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GroupRepository(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val groupDao = AppDatabase.getDatabase(context).groupDao()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // Get all groups with LiveData
    fun getAllGroups(): LiveData<List<Group>> {
        // Fetch from Firestore and update Room
        refreshGroups()

        // Return LiveData from Room
        return groupDao.getAllGroups().map { entities ->
            entities.map { it.toGroup() }
        }
    }

    // Get a single group by ID
    fun getGroupById(groupId: String): LiveData<Group> {
        val result = MutableLiveData<Group>()

        coroutineScope.launch {
            try {
                // Try to get from Room first
                val localData = groupDao.getGroupById(groupId)

                // If not in Room, fetch from Firestore
                if (localData.value == null) {
                    val document = db.collection("groups").document(groupId).get().await()
                    if (document.exists()) {
                        val members = document.get("members") as? List<String> ?: listOf()

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

                        // Save to Room
                        groupDao.insertGroup(GroupEntity.fromGroup(group))

                        withContext(Dispatchers.Main) {
                            result.value = group
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        result.value = localData.value?.toGroup()
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }

        return result
    }

    // Refresh groups from Firestore and update Room
    private fun refreshGroups() {
        coroutineScope.launch {
            try {
                val snapshot = db.collection("groups").get().await()

                val groups = snapshot.documents.map { document ->
                    val members = document.get("members") as? List<String> ?: listOf()

                    val createdBy = when (val createdByValue = document.get("createdBy")) {
                        is String -> createdByValue
                        else -> ""
                    }

                    Group(
                        id = document.id,
                        groupName = document.getString("groupName") ?: document.getString("name") ?: "",
                        description = document.getString("description") ?: "",
                        createdBy = createdBy,
                        createdAt = document.getLong("createdAt") ?: 0,
                        imageUrl = document.getString("imageUrl"),
                        currency = document.getString("currency") ?: "USD",
                        members = members
                    )
                }

                // Save to Room
                val entities = groups.map { GroupEntity.fromGroup(it) }
                groupDao.insertGroups(entities)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // Create a new group
    suspend fun createGroup(group: Group): Boolean {
        return try {
            // Create in Firestore
            val groupMap = hashMapOf(
                "groupName" to group.groupName,
                "description" to group.description,
                "createdBy" to group.createdBy,
                "createdAt" to group.createdAt,
                "currency" to group.currency,
                "imageUrl" to (group.imageUrl ?: "default"),
                "members" to group.members
            )

            db.collection("groups").document(group.id).set(groupMap).await()

            // Save to Room
            groupDao.insertGroup(GroupEntity.fromGroup(group))
            true
        } catch (e: Exception) {
            false
        }
    }

    // Update a group
    suspend fun updateGroup(group: Group): Boolean {
        return try {
            // Update in Firestore
            val updates = hashMapOf<String, Any>(
                "groupName" to group.groupName,
                "description" to group.description,
                "currency" to group.currency,
                "imageUrl" to (group.imageUrl ?: "default")
            )

            db.collection("groups").document(group.id).update(updates).await()

            // Update in Room
            groupDao.insertGroup(GroupEntity.fromGroup(group))
            true
        } catch (e: Exception) {
            false
        }
    }

    // Join or leave a group
    suspend fun updateGroupMembers(groupId: String, members: List<String>): Boolean {
        return try {
            // Update in Firestore
            db.collection("groups").document(groupId).update("members", members).await()

            // Update in Room
            val group = groupDao.getGroupById(groupId).value
            if (group != null) {
                val updatedGroup = group.copy(members = members)
                groupDao.insertGroup(updatedGroup)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}