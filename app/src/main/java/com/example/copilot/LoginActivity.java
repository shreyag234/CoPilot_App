package com.example.copilot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.security.MessageDigest;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    EditText email, password;
    TextView name, goBackRegister, forgotPw;
    Button loginBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        name = findViewById(R.id.getName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        goBackRegister = findViewById(R.id.signUp_btn);
        forgotPw = findViewById(R.id.ForgotPw_btn);
        loginBtn = findViewById(R.id.Login_btn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginUser();
            }
        });

        goBackRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void loginUser() {
        String UEmail = email.getText().toString().trim();
        String UPassword = password.getText().toString().trim();
        String UHassPass = sha256(UPassword);

        //validation
        if(UEmail.isEmpty()){
            email.setError("Email address is required!");
            email.requestFocus();
            return;
        }
        //checking if the email is a valid email
        if(!Patterns.EMAIL_ADDRESS.matcher(UEmail).matches()){
            email.setError("Please provide a valid email");
            email.requestFocus();
            return;
        }
        if(UPassword.isEmpty()){
            password.setError("Set a password");
            password.requestFocus();
            return;
        }
        if(UPassword.length() < 6){
            password.setError("The password must be more than 6 characters!");
            password.requestFocus();
            return;
        }
        //checks if the password has an uppercase
        if(!UPassword.matches(".*[A-Z]*.")){
            password.setError("Password must contain an uppercase letter");
            password.requestFocus();
            return;
        }
        //checks if the password has special characters
        if(UPassword.matches("^(?=.*[_.$@]).*$")){
            password.setError("Password must not contain special characters ");
            password.requestFocus();
            return;
        }
        //checking if the login credentials are correct
         mAuth.signInWithEmailAndPassword(UEmail, UPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
             @Override
             public void onComplete(@NonNull Task<AuthResult> task) {
                 if (task.isSuccessful()){

                     //redirect to the weather page
                     Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_LONG).show();
                     startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                     }else{
                      Toast.makeText(LoginActivity.this, "Login Credentials are incorrect!", Toast.LENGTH_LONG).show();
                     }
             }
         });


    }

    public static String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
//code attribution
//This code is similar to a youtube video
//YouTube channel: https://www.youtube.com/channel/UCSuhJQ4PTXOpT7Ikt4aUAkg
// https://www.youtube.com/watch?v=dfTzz5AhBNQ&t=5s