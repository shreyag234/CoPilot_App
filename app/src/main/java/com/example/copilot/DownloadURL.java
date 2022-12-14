package com.example.copilot;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class DownloadURL {
    public String readUrl(String myurl) throws IOException{
        String data = "";
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        try{
            //creating, opening and connecting the url
            URL url = new URL(myurl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            inputStream = urlConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb = new StringBuffer();

            String line = "";
            while((line = bufferedReader.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();
            Log.d("url", urlConnection.toString());
            bufferedReader.close();
        }catch (MalformedURLException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }finally{

            inputStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}
