package com.example.androidproject.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import com.example.androidproject.databinding.ActivityRegisterBinding;
import com.example.androidproject.model.User;
import com.example.androidproject.repositories.UserRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        onRegister();
        binding.registerProgressBar.setVisibility(View.GONE);
    }

    private void onRegister() {
        binding.registerBtn.setOnClickListener(View -> {
            binding.registerProgressBar.setVisibility(View.VISIBLE);
            String password, mail, username, avatar;
            password = String.valueOf(binding.passwordTpRegister.getText());
            username = String.valueOf(binding.usernameTpRegister.getText());
            mail = String.valueOf(binding.mailTpRegister.getText());
            avatar = "";
            if(!isFieldsEmpty(password, mail, username)){
                User userToAdd = new User(password, mail,username , UserRepository.instance.userId, avatar);

                saveUser(userToAdd);
            }
        });
    }

    private boolean isFieldsEmpty(String password,  String mail,
                                  String username){

        if(TextUtils.isEmpty(password)){
            Toast.makeText(RegisterActivity.this, "enter password", Toast.LENGTH_SHORT).show();
            return  true;
        }
        if(TextUtils.isEmpty(username)){
            Toast.makeText(RegisterActivity.this, "enter username", Toast.LENGTH_SHORT).show();
            return  true;
        }
        if(TextUtils.isEmpty(mail)){
            Toast.makeText(RegisterActivity.this, "enter mail", Toast.LENGTH_SHORT).show();
            return  true;
        }

        return false;
    }

    private void changeActivity(Class activityClass) {
        Intent intent = new Intent(RegisterActivity.this, activityClass);
        startActivity(intent);
        finish();
    }

    private void saveUser(User userToAdd) {
        mAuth.createUserWithEmailAndPassword(userToAdd.getMail(), userToAdd.getPassword())
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            userToAdd.setUid(uid);
                            UserRepository.instance.setUserId(uid);
                            UserRepository.instance.createUser(userToAdd, (unused) -> changeActivity(MainActivity.class));
                        } else {
                            Toast.makeText(RegisterActivity.this, "can not sign you",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
