package com.example.androidproject.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.androidproject.model.Group

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey
    val id: String,
    val groupName: String,
    val description: String,
    val createdBy: String,
    val createdAt: Long,
    val imageUrl: String?,
    val currency: String,
    val members: List<String>
) {
    fun toGroup(): Group {
        return Group(
            id = id,
            groupName = groupName,
            description = description,
            createdBy = createdBy,
            createdAt = createdAt,
            imageUrl = imageUrl,
            currency = currency,
            members = members
        )
    }

    companion object {
        fun fromGroup(group: Group): GroupEntity {
            return GroupEntity(
                id = group.id,
                groupName = group.groupName,
                description = group.description,
                createdBy = group.createdBy,
                createdAt = group.createdAt,
                imageUrl = group.imageUrl,
                currency = group.currency,
                members = group.members
            )
        }
    }
}