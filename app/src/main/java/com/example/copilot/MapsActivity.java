package com.example.copilot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationRequest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMarkerClickListener{

    private FirebaseAuth mAuth;
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    private LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    Marker mCurrLocationMarker;
    BottomNavigationView navigationView;
    boolean firstRun = true;
    public static final int Request_Code = 99;
    int PROXIMITY_RADIUS = 10000;
    double latitude_rn, longitude_rn;

    FusedLocationProviderClient fusedLocationProviderClient;
    Location newCurrentLocationtouse;
    double newCurrentLocLat, newCurrentLocLng;

    ImageButton atm, attractions, hotel, hospital, parks, restaurant;
    ImageButton searchBtn;
    ImageButton zoomIn, zoomOut;
    ImageButton shareBtn;
    EditText searchText;
    Button saveLocation;
    FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference, userLocationRef;

    Object dataTransfer[] = new Object[2];
    GetNearByPlaces getNearByPlaces = new GetNearByPlaces();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        reference = FirebaseDatabase.getInstance().getReference("Places");
        userLocationRef = FirebaseDatabase.getInstance().getReference("User").child(mAuth.getUid()).child("Saved Locations");

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
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //hooks
        searchBtn = findViewById(R.id.searchBtn);
        searchText = findViewById(R.id.searchPlace);

        atm = findViewById(R.id.atm);
        attractions = findViewById(R.id.attractions);
        hotel = findViewById(R.id.hotel);
        hospital = findViewById(R.id.hospital);
        parks = findViewById(R.id.forest);
        restaurant = findViewById(R.id.restaurant);

        zoomIn = findViewById(R.id.zoomInBtn);
        zoomOut = findViewById(R.id.zoomOutBtn);
        shareBtn = findViewById(R.id.shareBtn);
        saveLocation = findViewById(R.id.SaveLoc);
        navigationView = findViewById(R.id.bottom_nav);

        saveLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Save_Retrieve(); //calling the method that saves the suer location to Firebase
            }
        });
        //the below allows the user to share their location coordinates to other apps
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User").child(mAuth.getUid()).child("Current Location");
                ValueEventListener listener = ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Double send_lat = snapshot.child("lat").getValue(Double.class);
                        Double send_lng = snapshot.child("lng").getValue(Double.class);
                        Toast.makeText(MapsActivity.this, "lat:"+send_lat.toString()+"lng:"+send_lng.toString(), Toast.LENGTH_LONG).show();

                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_TEXT, "My Location >> latitude:"+send_lat.toString()+"longitude:"+send_lng.toString());
                        shareIntent.setType("text/plain");

                        Intent sendIntent = Intent.createChooser(shareIntent, null);
                        startActivity(sendIntent);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });

        zoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });
        navigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.maps_nav:
                    //redirect to routes page
                    startActivity(new Intent(MapsActivity.this, RoutesActivity.class));
                    break;
                case R.id.fav_nav:
                    //redirect to favs page
                    startActivity(new Intent(MapsActivity.this, FavActivity.class));
                    break;
                case R.id.home_nav:
                    item.setChecked(true);
                    //redirect to dashboard
                    Intent i = new Intent(this, MapsActivity.class);
                    startActivity(i);
                    break;
                case R.id.settings_nav:
                    //redirect to settings page
                    startActivity(new Intent(MapsActivity.this, SettingsActivity.class));
                    break;
                case R.id.logoff_nav:
                    //exit the app or redirect to register
                    mAuth.signOut();
                    startActivity(new Intent(MapsActivity.this, RegisterActivity.class));
                    break;
            }
            return true;
        }

        );
        //on click listeners for all searches
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText tf_location = searchText;
                String location = tf_location.getText().toString();
                List<Address> addressList = null;
                MarkerOptions markerOptions = new MarkerOptions();

                if(! location.equals("")){
                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    try{
                        addressList = geocoder.getFromLocationName(location, 5);
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                    for(int x=0; x < addressList.size(); x++){
                        Address myAddress = addressList.get(x);
                        LatLng latLng = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());
                        markerOptions.position(latLng);
                        mGoogleMap.addMarker(markerOptions);
                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    }
                }
            }
        });
        atm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleMap.clear();
                String atm = "atm";
                String url = getUrl(latitude_rn, longitude_rn, atm);

                dataTransfer[0] = mGoogleMap;
                dataTransfer[1] = url;

                getNearByPlaces.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing nearby ATMs", Toast.LENGTH_LONG).show();

            }
        });
        hospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleMap.clear();
                String hospital = "hospital";
                String url = getUrl(latitude_rn,longitude_rn, hospital);

                dataTransfer[0] = mGoogleMap;
                dataTransfer[1] = url;
                getNearByPlaces = new GetNearByPlaces();
                getNearByPlaces.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing nearby Hospitals", Toast.LENGTH_LONG).show();
            }
        });

        hotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleMap.clear();
                String spa = "spa";
                String url = getUrl(latitude_rn, longitude_rn, spa);

                dataTransfer[0] = mGoogleMap;
                dataTransfer[1] = url;
                getNearByPlaces = new GetNearByPlaces();
                getNearByPlaces.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing nearby Hotels", Toast.LENGTH_LONG).show();
            }
        });

        parks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleMap.clear();
                String park = "park";
                String url = getUrl(latitude_rn, longitude_rn, park);

                dataTransfer[0] = mGoogleMap;
                dataTransfer[1] = url;
                getNearByPlaces = new GetNearByPlaces();
                getNearByPlaces.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing nearby Parks", Toast.LENGTH_LONG).show();
            }
        });

        restaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleMap.clear();
                String restaurant = "restaurant";
               String url = getUrl(latitude_rn, longitude_rn, restaurant);

                dataTransfer[0] = mGoogleMap;
                dataTransfer[1] = url;
                getNearByPlaces = new GetNearByPlaces();
                getNearByPlaces.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing nearby Restaurants", Toast.LENGTH_LONG).show();
            }
        });

        attractions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleMap.clear();
                String tourist_attractions= "tourist_attractions";
                String url = getUrl(latitude_rn, longitude_rn, tourist_attractions);

                dataTransfer[0] = mGoogleMap;
                dataTransfer[1] = url;
                getNearByPlaces = new GetNearByPlaces();
                getNearByPlaces.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing nearby Attractions", Toast.LENGTH_LONG).show();
            }
        });

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


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
            //mGoogleApiClient.connect();

        }

    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    private String getUrl(double latitude, double longitude, String nearByPlace){
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+mGoogleMap.getMyLocation().getLatitude()+","+mGoogleMap.getMyLocation().getLongitude());
        googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type="+nearByPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key="+"AIzaSyBwZVqC8QGNdxtJQHqGv9OYz9QqenZ800Q");

        return (googlePlaceUrl.toString());
    }

    @Override
    public void onConnected(Bundle bundle) {
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
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MapsActivity.this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(@NonNull Location location)
    {
        mLastLocation= location;
        //checking if the marker already exists or not
       if(mCurrLocationMarker != null){
          // saveLocation(location);
          mCurrLocationMarker.remove(); //removing the marker
        }
        LatLng latLng = new LatLng(mGoogleMap.getMyLocation().getLatitude(), mGoogleMap.getMyLocation().getLongitude());
        latitude_rn = mLastLocation.getLatitude();
        longitude_rn = mLastLocation.getLongitude();
       // Log.d("helpppp", latitude_rn+" "+longitude_rn+" is your current position");
        Toast.makeText(MapsActivity.this, "Location : "+mLastLocation.getLatitude()+""+mLastLocation.getLongitude(), Toast.LENGTH_LONG).show();

        MarkerOptions markerOptions = new MarkerOptions();
        //setting the marker properties
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        //markerOptions.visible(true);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_icon));

        mGoogleMap.addMarker(markerOptions);
        CameraPosition campos = new CameraPosition.Builder()
                .target(latLng)
                        .zoom(15)
                                .bearing(0)
                                        .build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(campos));


        if(mGoogleApiClient != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        }
    }

    private void saveLocation(Location location) {
        reference.setValue(location);
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
                        mGoogleMap.setMyLocationEnabled(true);
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
        return false;
    }

    private void Save_Retrieve(){

        HashMap Clocation = new HashMap();
        Clocation.put("lat", mGoogleMap.getMyLocation().getLatitude());
        Clocation.put("lng", mGoogleMap.getMyLocation().getLongitude());
        FirebaseDatabase.getInstance().getReference("User").child(mAuth.getUid()).child("Current Location").setValue(Clocation);
        Toast.makeText(this, "Saved Location!", Toast.LENGTH_LONG).show();
    }

}

