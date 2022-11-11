package com.example.copilot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutesActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener{

    private GoogleMap mMap;
    private FirebaseAuth mAuth;

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;

    ImageButton zoomIn, zoomOut, searchBtn;
    BottomNavigationView navigationView;
    EditText place;
    Button go_btn, show_btn, route_btn, save_btn;


    public static final int Request_Code = 99;
    int PROXIMITY_RADIUS = 10000;
    double latitude, longitude;
    //the below holds the destinaiton methods
    double end_latitude, end_longitude;
    double lat_end_new, lng_end_new;

    //varibales to store the firebase reference and show markers
    private DatabaseReference refDatabase;

    //arrays that store all the latitudes and longitudes of the fav location
    ArrayList<Double> fav_lat = new ArrayList<Double>();
    ArrayList<Double> fav_lng = new ArrayList<Double>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_routes);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            checkLocPermission();
        }
        //checks if Google Play Services are available or not
        if(!CheckGooglePlayServices()){
            Log.d("onCreate", "Finishing test case since Google Play Services are not available");
            finish();
        }else{
            Log.d("onCreate", "Google Play Services available");
        }

        zoomIn = findViewById(R.id.zoomInBtn);
        zoomOut = findViewById(R.id.zoomOutBtn);
        searchBtn = findViewById(R.id.searchBtn);
        navigationView = findViewById(R.id.bottom_nav);
        place = findViewById(R.id.searchPlace);
        go_btn = findViewById(R.id.time_btn); //shows the distance and duration
        show_btn = findViewById(R.id.go_btn); //makes the marker appear
        route_btn = findViewById(R.id.route_btn); //embedded route on map
        save_btn = findViewById(R.id.save_btn);

        getFavLat();
        getFavLng();

        zoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });

        //on click listeners for all searches
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText tf_location = place;
                String location = tf_location.getText().toString();
                List<Address> addressList = null;
                MarkerOptions markerOptions = new MarkerOptions();

                if(! location.equals("")) {
                    Geocoder geocoder = new Geocoder(RoutesActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 5);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    for (int x = 0; x < addressList.size(); x++) {
                        Address myAddress = addressList.get(x);
                        LatLng latLngNew = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());

                        lat_end_new = myAddress.getLatitude();
                        lng_end_new = myAddress.getLongitude();

                        markerOptions.position(latLngNew);
                        mMap.addMarker(markerOptions);
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLngNew));
                    }

                }


            }
        });

        navigationView.setOnItemSelectedListener(item -> {
                    switch (item.getItemId()){
                        case R.id.maps_nav:
                            //redirect to routes page
                            startActivity(new Intent(RoutesActivity.this, RoutesActivity.class));
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
                            startActivity(new Intent(RoutesActivity.this, SettingsActivity.class));
                            break;
                        case R.id.logoff_nav:
                            //exit the app or redirect to register
                            mAuth.signOut();
                            startActivity(new Intent(RoutesActivity.this, RegisterActivity.class));
                            break;
                    }
                    return true;
                }

        );

        show_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                // LatLng endLatLng = new LatLng(end_latitude, end_longitude);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(end_latitude, end_longitude));
                markerOptions.title("Destination");
                markerOptions.draggable(true);

                //calculating the distance

                float results[] = new float[1];
                Location.distanceBetween(latitude, longitude, end_latitude, end_longitude, results);

                // Double distance = SphericalUtil.computeDistanceBetween(currentLatLng, endLatLng);
                String kilometers = String.format("%.2f", results[0] / 1000);
                String miles = String.format("%.2f", (results[0] / 1000) * 0.62137);
                markerOptions.snippet("Distance = " + kilometers);
                mMap.addMarker(markerOptions);

                //adding the distance to the textview
                // display.setText(kilometers +" Kms" +
                // "\n"+miles +" in Miles");




            }
        });

        go_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Object dataTransfer[] = new Object[3];
                String url = getdirectionsUrl();
                //GetDirectionsDAta getDirectionsDAta = new GetDirectionsDAta();
                GetDistanceData getDistanceData = new GetDistanceData();
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = new LatLng(end_latitude, end_longitude);

                //getDirectionsDAta.execute(dataTransfer);
                getDistanceData.execute(dataTransfer);


               // Toast.makeText(RoutesActivity.this, "Distance: "+ getDistanceData.distance +"\n Duration :" +
                     //   getDistanceData.duration, Toast.LENGTH_LONG).show();
            }
        });

        route_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object dataTransfer[] = new Object[3];
                String url = getdirectionsUrl();
                GetDirectionsDAta getDirectionsDAta = new GetDirectionsDAta();
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = new LatLng(end_latitude, end_longitude);

                getDirectionsDAta.execute(dataTransfer);


            }
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //calling the method that saves location
                saveToFirebase();
                Intent intent = new Intent(RoutesActivity.this, FavActivity.class);
                intent.putExtra("key1", fav_lat);
                intent.putExtra("key2", fav_lng);
                startActivity(intent);
            }
        });

                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private String getdirectionsUrl() {

        StringBuilder googleDirectionsUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin="+mMap.getMyLocation().getLatitude()+","+mMap.getMyLocation().getLongitude());
        googleDirectionsUrl.append("&destination="+end_latitude+","+end_longitude);
        googleDirectionsUrl.append("&key="+"AIzaSyBwZVqC8QGNdxtJQHqGv9OYz9QqenZ800Q");
        Log.d("url",googleDirectionsUrl.toString());
        return googleDirectionsUrl.toString();

       // https://maps.googleapis.com/maps/api/directions/json?origin=-26.029670,28.062630&destination=-26.084960,28.015360&key=AIzaSyBwZVqC8QGNdxtJQHqGv9OYz9QqenZ800Q
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }


    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS){
            if(googleAPI.isUserResolvableError(result)){
                googleAPI.getErrorDialog(this,result, 0).show();
            }
            return false;
        }
        return true;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLastLocation = location;
        //checking if the marker already exists or not
        if(mCurrLocationMarker != null){
            mCurrLocationMarker.remove(); //removing the marker
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        MarkerOptions markerOptions = new MarkerOptions();
        //setting the marker properties
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.visible(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        mCurrLocationMarker = mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(15));


        if(mGoogleApiClient != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        }
    }

    public boolean checkLocPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_Code);
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_Code);
            }
            return false;
        }
        else {
            return true;
        }
    }

    @SuppressWarnings("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case Request_Code:
                //check if the permission is granted or not
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permission is granted
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        if(mGoogleApiClient == null){
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }else{
                    //if permission is denied
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
       //making the marker draggable
        marker.setDraggable(true);
        marker.getTitle();

        return false;
    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        end_latitude = marker.getPosition().latitude;
        end_longitude = marker.getPosition().longitude;
        //saveToFirebase();


    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {

    }


    private void saveToFirebase() {
        HashMap<String, Object> location = new HashMap<String, Object>();
        fav_lat.add(end_latitude);
        fav_lng.add(end_longitude);
        location.put("lat", fav_lat);
        location.put("lng", fav_lng);

        FirebaseDatabase.getInstance().getReference("User").child(mAuth.getUid()).child("Location").setValue(location);
        Toast.makeText(this, "Favourite Added!", Toast.LENGTH_LONG).show();

    }

    public void getFavLng()
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
        ref.child(mAuth.getUid()).child("Location").child("lng")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<Double> friends = new ArrayList<>();
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            String friend = ds.getValue(Double.class).toString();
                            friends.add(Double.parseDouble(friend));
                        }
                        fav_lng = friends;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }
    public void getFavLat()
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
        ref.child(mAuth.getUid()).child("Location").child("lat")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<Double> friends = new ArrayList<>();
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            String friend = ds.getValue(Double.class).toString();
                            friends.add(Double.parseDouble(friend));
                        }
                        fav_lat = friends;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }

}


