package com.example.copilot;

import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class GetDirectionsDAta extends AsyncTask<Object, String, String> {
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
       // HashMap<String, String> directionsList = null;
        String[] directionsList2;
        DataParse parser = new DataParse();
        directionsList2 = parser.parseDirections(s);
        displayDirection(directionsList2);

        /**
        duration = directionsList.get("duration");
        distance = directionsList.get("distance");

        mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(true);
        markerOptions.title("Duration = "+duration);
        markerOptions.snippet("Distance = "+distance);


        mMap.addMarker(markerOptions);**/

    }

    public void displayDirection(String[] directionsList){
        int count = directionsList.length;
        for(int i = 0; i<count; i++){
            PolylineOptions options = new PolylineOptions();
            options.color(Color.BLUE);
            options.width(10);
            options.addAll(PolyUtil.decode(directionsList[i]));

            mMap.addPolyline(options);
        }
    }
}
//code attribute
//similar to YouTube Video
// YouTube Channel: https://www.youtube.com/c/MdJamalmca
//https://www.youtube.com/watch?v=kRAyXxgwOhQ&t=830s

//YouTube Channel: https://www.youtube.com/user/jummanhasan
//https://www.youtube.com/watch?v=rjucwwCU3E4&t=326s

//YouTube Channel: https://www.youtube.com/user/kishorprogamer
//https://www.youtube.com/watch?v=_xqgQ-Dw9PM&t=225s