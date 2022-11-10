package com.example.copilot;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class GetNearByPlaces extends AsyncTask<Object,String, String> {
    String googlePlacesData;
    GoogleMap mMap;
    String url;
    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap) objects[0];
        url = (String) objects[1];

        DownloadURL downloadURL = new DownloadURL();
        try {
            googlePlacesData = downloadURL.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s) {
       /** List<HashMap<String, String>> nearByPlacesList = null;
        DataParse parser = new DataParse();
        nearByPlacesList = parser.parse(s);
        ShowPlaces(nearByPlacesList);**/

       try{
           JSONObject jsonObject = new JSONObject(s);
           JSONArray jsonArray = jsonObject.getJSONArray("results");

           for(int i=0; i< jsonArray.length(); i++){
               JSONObject jsonObject1 = jsonArray.getJSONObject(i);
               JSONObject getLocation = jsonObject1.getJSONObject("geometry")
                       .getJSONObject("location");

               String lat = getLocation.getString("lat");
               String lng = getLocation.getString("lng");

               JSONObject getName = jsonArray.getJSONObject(i);
               String name = getName.getString("name");

               mMap.clear();
               LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
               MarkerOptions markerOptions = new MarkerOptions();
               markerOptions.title(name);
               markerOptions.position(latLng);
               mMap.addMarker(markerOptions);
               mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
           }
           List<HashMap<String, String>> nearByPlacesList = null;
           DataParse parse = new DataParse();
           nearByPlacesList  = parse.parse(s);
           ShowPlaces(nearByPlacesList);
       }catch (JSONException e){
           e.printStackTrace();
       }
    }

    private void ShowPlaces(List<HashMap<String, String>> nearByPlacesList){
        for (int i =0; i < nearByPlacesList.size(); i++){
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearByPlacesList.get(i);

            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            Double lat = Double.parseDouble(googlePlace.get("lat"));
            Double lng = Double.parseDouble(googlePlace.get("lng"));

            LatLng latLng = new LatLng(lat,lng);
            markerOptions.position(latLng);
            markerOptions.title(placeName + " : "+ vicinity);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomBy(30));

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    GetDirectionsDAta getDirectionsDAta = new GetDirectionsDAta();
                    Object dataTransfer[] = new Object[3];
                   // mMap.clear();
                    String url =  "https://maps.googleapis.com/maps/api/directions/json?origin="+mMap.getMyLocation().getLatitude()+","+mMap.getMyLocation().getLongitude()+
                            "&destination="+marker.getPosition().latitude+","+marker.getPosition().longitude+"&key=AIzaSyBwZVqC8QGNdxtJQHqGv9OYz9QqenZ800Q";

                    dataTransfer[0] = mMap;
                    dataTransfer[1] = url;
                    dataTransfer[2] = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);

                    getDirectionsDAta.execute(dataTransfer);

                    return false;

                }
            });

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