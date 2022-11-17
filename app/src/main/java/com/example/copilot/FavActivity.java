package com.example.copilot;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.copilot.databinding.ActivityFavBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class FavActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityFavBinding binding;
    private FirebaseAuth mAuth;

    BottomNavigationView navigationView;

    ArrayList<Double> passedLat = new ArrayList<Double>();
    ArrayList<Double> passedLng = new ArrayList<Double>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        navigationView = findViewById(R.id.bottom_nav);

        navigationView.setOnItemSelectedListener(item -> {
                    switch (item.getItemId()){
                        case R.id.maps_nav:
                            //redirect to routes page
                            startActivity(new Intent(FavActivity.this, RoutesActivity.class));
                            break;
                        case R.id.fav_nav:
                            //redirect to favs page
                            startActivity(new Intent(FavActivity.this, FavActivity.class));
                            break;
                        case R.id.home_nav:
                            item.setChecked(true);
                            //redirect to dashboard
                            Intent i = new Intent(this, MapsActivity.class);
                            startActivity(i);
                            break;
                        case R.id.settings_nav:
                            //redirect to settings page
                            startActivity(new Intent(FavActivity.this, SettingsActivity.class));
                            break;
                        case R.id.logoff_nav:
                            //exit the app or redirect to register
                            mAuth.signOut();
                            startActivity(new Intent(FavActivity.this, RegisterActivity.class));
                            break;
                    }
                    return true;
                }

        );
        passedLat = (ArrayList<Double>) getIntent().getSerializableExtra("key1");
        passedLng = (ArrayList<Double>) getIntent().getSerializableExtra("key2");

        binding = ActivityFavBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //adding fav location markers
        for(int i=0; i < passedLat.size(); i++){
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(passedLat.get(i), passedLng.get(i)) ).title("My Favorite")
            );
        }

    }
}