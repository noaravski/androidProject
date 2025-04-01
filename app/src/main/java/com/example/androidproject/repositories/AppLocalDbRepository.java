package com.example.androidproject.repositories;

import android.graphics.Bitmap;

import androidx.room.Database;
import androidx.room.RoomDatabase;

//import com.example.androidproject.dao.ReviewDao;
//import com.example.androidproject.model.Review;

//@Database(entities = {Review.class}, version = 2)
public abstract class AppLocalDbRepository extends RoomDatabase {
//    public abstract ReviewDao reviewDao();

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