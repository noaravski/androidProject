package com.example.androidproject.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.androidproject.model.Review;
import com.example.androidproject.repositories.ReviewRepository;
import java.util.List;

public class UserProfileFragmentViewModel extends ViewModel {
    private LiveData<List<Review>> reviewList = ReviewRepository.instance.getUserReviews();

    public LiveData<List<Review>> getReviewListData(){
        return reviewList;
    }
}
