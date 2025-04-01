package com.example.androidproject.repositories;

import android.graphics.Bitmap;

import androidx.room.Database;
import androidx.room.RoomDatabase;

public abstract class AppLocalDbRepository extends RoomDatabase {

    public static class ImageRepository {
        final public static ImageRepository instance = new ImageRepository();
        private UserRepository.FbImgRepository fbImgModel = new UserRepository.FbImgRepository();


        public interface UploadImageListener{
            void onComplete(String uri);
        }
        public void uploadImage(String name, Bitmap bitmap, UploadImageListener callback) {
            fbImgModel.uploadImage(name, bitmap, callback);
        }

    }
}