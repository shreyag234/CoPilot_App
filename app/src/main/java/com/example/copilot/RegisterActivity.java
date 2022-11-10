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
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    EditText name, email, password;
    Button registerBtn;
    TextView signIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        name = findViewById(R.id.name_txt);
        email = findViewById(R.id.email_txt);
        password = findViewById(R.id.password_txt);
        registerBtn = findViewById(R.id.Register_btn);
        signIn = findViewById(R.id.signIn_btn);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
    }

    private void registerUser() {
        //converting the text box inputs to a string
        String Name = name.getText().toString().trim();
        String Email = email.getText().toString().trim();
        String Password = password.getText().toString().trim();

        //validations
        //error messages
        if(Name.isEmpty()){
            name.setError("Name is required!");
            name.requestFocus();
            return;
        }
        if(Email.isEmpty()){
            email.setError("Email address is required!");
            email.requestFocus();
            return;
        }
        //checking if the email is a valid email
        if(!Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
            email.setError("Please provide a valid email");
            email.requestFocus();
            return;
        }
        if(Password.isEmpty()){
            password.setError("Set a password");
            password.requestFocus();
            return;
        }
        if(Password.length() < 6){
            password.setError("The password must be more than 6 characters!");
            password.requestFocus();
            return;
        }
        //checks if the password has an uppercase
        if(!Password.matches(".*[A-Z]*.")){
            password.setError("Password must contain an uppercase letter");
            password.requestFocus();
            return;
        }
        //checks if the password has special characters
        if(Password.matches("^(?=.*[_.$@]).*$")){
            password.setError("Password must not contain special characters ");
            password.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //checking if the user is registered
                        if(task.isSuccessful()){
                            //creating an object of User

                            User userObj = new User(Name, Email, Password);

                            //after this add realtime database to this app
                            FirebaseDatabase.getInstance().getReference("User")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(userObj).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            //checking again if the user is registered
                                            if(task.isSuccessful()){
                                                Toast.makeText(RegisterActivity.this, "User has been successfully registered!!",
                                                        Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));

                                            }else{
                                                Toast.makeText(RegisterActivity.this, "Failed to register! TRY AGAIN",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        }
                    }
                });

    }
}
//code attribution
//This code is similar to a youtube video
//YouTube channel: https://www.youtube.com/channel/UCSuhJQ4PTXOpT7Ikt4aUAkg
// https://www.youtube.com/watch?v=dfTzz5AhBNQ&t=5s