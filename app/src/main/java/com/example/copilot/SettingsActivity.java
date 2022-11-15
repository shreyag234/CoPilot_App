package com.example.copilot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;

import java.util.Locale;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

    private Switch darkModeSwitch;
    private FirebaseAuth mAuth;
    BottomNavigationView navigationView;
    RadioButton km, miles;
    RadioButton rbSpanish, rbAfrikaans;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    int i = 0;
    SettingsClass settingsClass = new SettingsClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (new DarkModePrefManager(this).isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        reference = firebaseDatabase.getInstance().getReference("User").child(mAuth.getUid()).child("Settings");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    i = (int) snapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        navigationView = findViewById(R.id.bottom_nav);
        km = findViewById(R.id.rbKm);
        miles = findViewById(R.id.rbMiles);

        rbAfrikaans = findViewById(R.id.Afrikaans);
        rbSpanish = findViewById(R.id.Spanish);

        km.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s1 = km.getText().toString();

                km.isChecked();
                settingsClass.setSetting(s1);
                //reference.child(String.valueOf(i+1)).setValue(settingsClass);
                reference.setValue(settingsClass);
            }
        });
        miles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                miles.isChecked();
                String s2 = miles.getText().toString();
                settingsClass.setSetting(s2);
               //reference.child(String.valueOf(i+1)).setValue(settingsClass);
                reference.setValue(settingsClass);
            }
        });

        rbAfrikaans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLocale("af");
            }
        });
        rbSpanish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLocale("es");
            }
        });
        navigationView.setOnItemSelectedListener(item -> {
                    switch (item.getItemId()){
                        case R.id.maps_nav:
                            //redirect to routes page
                            startActivity(new Intent(SettingsActivity.this, RoutesActivity.class));
                            break;
                        case R.id.fav_nav:
                            //redirect to favs page
                            break;
                        case R.id.home_nav:
                            item.setChecked(true);
                            //redirect to dashboard
                            Intent i = new Intent(this, MapsActivity.class);
                            startActivity(i);
                            break;
                        case R.id.settings_nav:
                            //redirect to settings page
                            startActivity(new Intent(SettingsActivity.this, SettingsActivity.class));
                            break;
                        case R.id.logoff_nav:
                            //exit the app or redirect to register
                            mAuth.signOut();
                            startActivity(new Intent(SettingsActivity.this, RegisterActivity.class));
                            break;
                    }
                    return true;
                }

        );

        //function for enabling dark mode
        setDarkModeSwitch();
    }
    private void setDarkModeSwitch() {
        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        darkModeSwitch.setChecked(new DarkModePrefManager(this).isNightMode());
        darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DarkModePrefManager darkModePrefManager = new DarkModePrefManager(SettingsActivity.this);
                darkModePrefManager.setDarkMode(!darkModePrefManager.isNightMode());
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                recreate();
            }
        });
    }

    private void setLocale(String language) {
        Resources resources = getResources();

        DisplayMetrics metrics = resources.getDisplayMetrics();

        Configuration configuration = resources.getConfiguration();

        configuration.locale = new Locale(language);

        resources.updateConfiguration(configuration, metrics);

        onConfigurationChanged(configuration);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        rbSpanish.setText("Spanish");
        rbAfrikaans.setText("Afrikaans");
    }
}

