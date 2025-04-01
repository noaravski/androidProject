package com.example.androidproject.repositories;

import com.example.androidproject.model.Model;
import com.example.androidproject.model.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class FbUserRepository {
    CollectionReference usersCollection;

    public FbUserRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false).build();
        db.setFirestoreSettings(settings);
        usersCollection  = db.collection("users");
    }

    public void getUserData(String userId, Model.Listener<User> listener) {
        usersCollection.document(userId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    User user = User.fromJson(task.getResult().getData());
                    listener.onComplete(user);
                }
            }
        });
    }

    public void createUser(User user, Model.Listener<Void> listener) {
        usersCollection.document(user.getUid()).set(user.toJson()).addOnCompleteListener((task) -> {
            listener.onComplete(null);
        });
    }

    public void updateUser(User user, Model.Listener<Void> listener) {
        usersCollection.document(user.getUid()).update(user.toJson()).addOnCompleteListener((task) -> {
            listener.onComplete(null);
        });
    }
}