package com.example.copilot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;
import java.util.Arrays;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    EditText name, email, password;
    Button registerBtn;
    TextView signIn;

    ImageButton googleSignIn;
    ImageButton facebookSignIn;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    CallbackManager callbackManager;
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

        googleSignIn = findViewById(R.id.google_signIn);
        facebookSignIn = findViewById(R.id.facebook_signIn);

        //facebook sigin
        facebookSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callbackManager = CallbackManager.Factory.create();
                LoginManager.getInstance().logInWithReadPermissions(RegisterActivity.this, Arrays.asList("public_profile"));
                LoginManager.getInstance().registerCallback(callbackManager,
                        new FacebookCallback<LoginResult>() {
                            @Override
                            public void onSuccess(LoginResult loginResult) {
                                handleFacebookAccessToken(loginResult.getAccessToken());
                            }

                            @Override
                            public void onCancel() {

                            }

                            @Override
                            public void onError(@NonNull FacebookException e) {

                            }
                        });
            }
        });


        //google sigin
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();

        gsc = GoogleSignIn.getClient(this, gso);

        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignIn();
            }
        });

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

    private void SignIn(){
        Intent intent = gsc.getSignInIntent();
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                task.getResult(ApiException.class);
                startActivity(new Intent(RegisterActivity.this, MapsActivity.class));
            }catch (ApiException e){
                e.printStackTrace();
            }
        }

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void registerUser() {
        //converting the text box inputs to a string
        String Name = name.getText().toString().trim();
        String Email = email.getText().toString().trim();
        String Password = password.getText().toString();
        String HassPassword = sha256(Password);

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

                            User userObj = new User(Name, Email, HassPassword);

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

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user){
        Intent intent = new Intent(RegisterActivity.this, MapsActivity.class);
        startActivity(intent);
        Toast.makeText(RegisterActivity.this, "Facebook Authentication success.",
                Toast.LENGTH_SHORT).show();
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