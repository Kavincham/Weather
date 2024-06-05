package com.example.weatherapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Adapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {



    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private TextView Latitude,Longitude,Address,Time, Weather;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Latitude = findViewById(R.id.Latitude);
        Longitude = findViewById(R.id.Longitude);
        Address = findViewById(R.id.Address);
        Time = findViewById(R.id.Time);
        Weather =findViewById(R.id.Weather);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        requestQueue = Volley.newRequestQueue(this);

        getLocation();

    }
    private void getLocation(){

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

        }
        else{
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if(task.isSuccessful() && task.getResult() != null){
                    Location location = task.getResult();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    Latitude.setText("Latitude: " + latitude);
                    Longitude.setText("Longitude: " + longitude);

                    getAddress(latitude, longitude);
                    getCurrentTime();
                    getWeatherData(latitude, longitude);

                }
                else{
                    Toast.makeText(MainActivity.this, "unable to get location ", Toast.LENGTH_SHORT).show();
                }
                                                                                }
        });
                }
            }

        private void getAddress(double latitude, double longitude){
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude,longitude, 1);
                if(addresses != null && !addresses.isEmpty()){
                    Address address = addresses.get(0);
                    Address.setText(("Address:" + address.getAddressLine(0)));
            }
                else{
                    Address.setText("Address: Not found");
                }

            }catch(IOException e){
                e.printStackTrace();
                Address.setText("Address Error");
        }
}
private void getCurrentTime(){
     SimpleDateFormat df = new SimpleDateFormat("yyy-MM-dd:mm:ss" ,Locale.getDefault());
     String currentTime = df.format(new Date());
     Time.setText("Time" + currentTime);
}
private void getWeatherData(double latitude, double longitude) {
    String apiKey = "1fb7b3ecead35ce65d30aab41b388abd";
    String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + apiKey + "&units=metric";

    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject main = response.getJSONObject("main");
                        double temp = main.getDouble("temp");
                        int humidity = main.getInt("humidity");
                        String weatherDescription = response.getJSONArray("weather").getJSONObject(0).getString("description");

                        Weather.setText("weather:" + temp + "°C, Humidity:  " + humidity+ "%, Description:" + weatherDescription);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Weather.setText(("weather : Error parsing data"));
                    }
                }
            }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            error.printStackTrace();
            Weather.setText(("Weather: Error retrieving data"));
        }
    });
    requestQueue.add(jsonObjectRequest);
}

public  void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if(requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getLocation();

        }else{
            Toast.makeText(this, "Permission rejected" , Toast.LENGTH_SHORT).show();
        }
    }
}
}





