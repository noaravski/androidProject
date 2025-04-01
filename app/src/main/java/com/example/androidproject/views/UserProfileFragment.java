package com.example.androidproject.views;

import static java.sql.DriverManager.println;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.androidproject.R;
import com.example.androidproject.ReviewRecyclerAdapter;
import com.example.androidproject.ReviewsListFragment;
import com.example.androidproject.activities.LoginActivity;
import com.example.androidproject.viewModels.UserProfileFragmentViewModel;
import com.example.androidproject.databinding.FragmentUserProfileBinding;
import com.example.androidproject.model.LiveDataEvents;
import com.example.androidproject.model.Review;
import com.example.androidproject.repositories.ReviewRepository;
import com.example.androidproject.model.User;
import com.example.androidproject.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

public class UserProfileFragment extends Fragment {
    FragmentUserProfileBinding binding;
    FirebaseAuth mAuth;
    UserProfileFragmentViewModel viewModel;
    User currUserData;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    public static UserProfileFragment newInstance(String param1, String param2) {
        UserProfileFragment fragment = new UserProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        println("hey?");
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserProfileBinding.inflate(inflater,container,false);
        View view = binding.getRoot();
        binding.userProgressBar.setVisibility(View.VISIBLE);
        ReviewsListFragment reviewListFragment = (ReviewsListFragment) getChildFragmentManager().findFragmentById(R.id.listContainer);

        ReviewRecyclerAdapter.OnItemClickListener reviewRowOnClickListener = new ReviewRecyclerAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(int pos) {
                Bundle bundle = new Bundle();
                Review rv = viewModel.getReviewListData().getValue().get(pos);
                bundle.putParcelable("Review", rv);
                bundle.putString("eventId", rv.getEventId());
                Navigation.findNavController(view).navigate(R.id.action_userProfileFragment_to_newReviewFragment, bundle);
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel parcel, int i) {

            }
        };

        viewModel.getReviewListData().observe(getViewLifecycleOwner(),list->{
            reviewListFragment.setParameters(list, reviewRowOnClickListener);
        });

        UserRepository.instance.getUserData(user -> {
            binding.usernameTv.setText(user.getFirstName() + " " + user.getLastName());
            binding.mailTv.setText(user.getMail());
            binding.bioTv.setText(user.getBio());
            currUserData = user;

            if(user.getImgUrl() != null && user.getImgUrl() != "") {
                Picasso.get().load(user.getImgUrl()).placeholder(R.drawable.bear).into(binding.avatarImg);
            } else {
                binding.avatarImg.setImageResource(R.drawable.bear);
            }

            binding.userProgressBar.setVisibility(View.GONE);
        });

        binding.editBtn.setOnClickListener(view1 -> {
            Bundle userBundle = new Bundle();
            userBundle.putParcelable("User", currUserData);
            Navigation.findNavController(view1).navigate(R.id.action_userProfileFragment_to_editUserProfileFragment, userBundle);
        });

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                changeActivity(LoginActivity.class);
            }
        });


        if (reviewListFragment != null) {
            reviewListFragment.setParameters(viewModel.getReviewListData().getValue(), reviewRowOnClickListener);
        }

        LiveDataEvents.instance().EventReviewListReload.observe(getViewLifecycleOwner(),unused->{
            ReviewRepository.instance.refreshAllUserReviews();
        });

        return view;
    }

    private void changeActivity(Class activityClass) {
        Intent intent = new Intent(getActivity(), activityClass);
        startActivity(intent);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this).get(UserProfileFragmentViewModel.class);
    }
}