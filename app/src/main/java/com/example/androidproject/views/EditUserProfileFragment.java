package com.example.androidproject.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.androidproject.R;
import com.example.androidproject.model.CloudinaryModel;
import com.example.androidproject.model.User;
import com.example.androidproject.databinding.FragmentEditUserProfileBinding;
import com.example.androidproject.repositories.UserRepository;
import com.squareup.picasso.Picasso;

public class EditUserProfileFragment extends Fragment {

    FragmentEditUserProfileBinding binding;
    ActivityResultLauncher<Void> cameraLauncher;
    Boolean isImgSelected = false;
    User user;

    public EditUserProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentEditUserProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        user = getArguments().getParcelable("User");

        if (user != null) {
            binding.usernameEditTp.setText(user.getUsername());
            binding.mailTp.setText(user.getMail());
        }

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), new ActivityResultCallback<Bitmap>() {
            @Override
            public void onActivityResult(Bitmap result) {
                if (result != null) {
                    binding.userImg.setImageBitmap(result);
                    isImgSelected = true;
                }
            }
        });

        if (user != null && user.getImgUrl() != null && !user.getImgUrl().isEmpty()) {
            Picasso.get().load(user.getImgUrl()).placeholder(R.drawable.profile).into(binding.userImg);
        } else {
            binding.userImg.setImageResource(R.drawable.profile);
        }

        onPhotoClick();
        onSave(view);

        return view;
    }

    private void onPhotoClick() {
        binding.userImg.setOnClickListener(v -> cameraLauncher.launch(null));
    }

    private void uploadImg(User user, Context context, View view) {
        binding.userImg.setDrawingCacheEnabled(true);
        binding.userImg.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) binding.userImg.getDrawable()).getBitmap();

        new CloudinaryModel().uploadImage(bitmap, user.getMail(), url -> {
            if (url != null && !url.isEmpty()) {
                user.setImgUrl(url);
            }
            saveUserNewData(user, view);
            return null;
        }, error -> {
            // Optional: show a Toast or log the error
            saveUserNewData(user, view);
            return null;
        });
    }

    public void onSave(View view) {
        binding.saveEditBtn.setOnClickListener(v -> {
            User editedUser = new User(user.getPassword(), binding.mailTp.getText().toString(), binding.usernameEditTp.getText().toString(), user.getUid(), user.getImgUrl());
            if (isImgSelected) {
                uploadImg(editedUser, requireContext(), view);
            } else {
                saveUserNewData(editedUser, view);
            }
        });
    }

    private void saveUserNewData(User editedUser, View view) {
        UserRepository.instance.updateUser(editedUser, unused -> Navigation.findNavController(view).navigate(R.id.action_editUserProfileFragment_to_profileFragment));
    }
}