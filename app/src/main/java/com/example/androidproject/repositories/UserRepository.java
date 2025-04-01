package com.example.androidproject.repositories;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.example.androidproject.model.Model;
import com.example.androidproject.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class UserRepository {
    public String userId;
    private FbUserRepository fbUserModel = new FbUserRepository();
    final public static UserRepository instance = new UserRepository();

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void getUserData(Model.Listener<User> callback) {
        fbUserModel.getUserData(userId, callback);
    }

    public void createUser(User user, Model.Listener<Void> callback) {
        fbUserModel.createUser(user, callback);
    }

    public void updateUser(User user, Model.Listener<Void>  callback) {
        fbUserModel.updateUser(user, callback);
    }

    public static class FbImgRepository {
        FirebaseStorage storage;

        public FbImgRepository() {
            storage = FirebaseStorage.getInstance();
        }

        public void uploadImage(String name, Bitmap bitmap, AppLocalDbRepository.ImageRepository.UploadImageListener listener){
            StorageReference storageRef = storage.getReference();
            StorageReference imagesRef = storageRef.child("images/"+name +".jpg");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = imagesRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    listener.onComplete(null);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imagesRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        listener.onComplete(uri.toString());
                    });
                }
            });
        }
    }
}