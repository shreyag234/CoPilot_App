package com.example.copilot;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.RadioButton;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;

public class GetDistanceData extends AsyncTask<Object, String, String> {
    GoogleMap mMap;
    String url;
    String googleDirectionsData;
    String duration, distance;
    LatLng latLng;

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap) objects[0];
        url = (String) objects[1];
        latLng = (LatLng) objects[2];

        DownloadURL downloadURL = new DownloadURL();
        try {
            googleDirectionsData = downloadURL.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return googleDirectionsData;
    }
    @Override
    protected void onPostExecute(String s) {
        HashMap<String, String> directionsList = null;

        DataParse parser = new DataParse();
        directionsList = parser.parseDirection(s);

        duration = directionsList.get("duration");
        distance = directionsList.get("distance");

        mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(true);
        markerOptions.title("Duration = "+duration);
        markerOptions.snippet("Distance = "+distance);
        mMap.addMarker(markerOptions);

    }
}
//code attribution
//similar to YouTube Video
// YouTube Channel: https://www.youtube.com/c/MdJamalmca
//https://www.youtube.com/watch?v=kRAyXxgwOhQ&t=830s

//YouTube Channel: https://www.youtube.com/user/jummanhasan
//https://www.youtube.com/watch?v=rjucwwCU3E4&t=326s

//YouTube Channel: https://www.youtube.com/user/kishorprogamer
//https://www.youtube.com/watch?v=_xqgQ-Dw9PM&t=225s