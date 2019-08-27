package com.example.benigps;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.benigps.Model.Model;
import com.example.benigps.service.Gps_api;
import com.example.benigps.utils.URLConstants;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GpsTracker extends Service implements LocationListener {

    Handler mHandler = new Handler();
    // flag for GPS status
    boolean isGPSEnabled = false;
    // flag for network status
    boolean isNetworkEnabled = false;
    JSONObject jsonObject;
    String id;
    public Runnable myRunnable;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; //
    double latitude,lattitudeA,lattitudeB; // latitude
    double longitude,longitudeA,longitudeB; // longitude

    boolean result;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES =2; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 30 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    ///Location to Get Distance
    Location locationA;
    Location locationB;

    SharedPreferences mPref;
    SharedPreferences.Editor medit;


    public GpsTracker() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        medit = mPref.edit();
        id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        RefreshUserLocation();

    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) getApplicationContext()
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return null;
                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();

                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();

                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GpsTracker.this);
        }
    }

    /**
     * Function to get latitude
     * */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getApplicationContext());

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getApplicationContext().startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }
    @Override
    public void onStart(Intent intent, int startid) {

        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        medit.putString("LATLNG", "").commit();
        mHandler.removeCallbacks(myRunnable);
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void RefreshUserLocation() {

        mHandler.postDelayed(myRunnable=new Runnable() {
            @Override
            public void run() {
                getLocation();
                try {
                    //Thread.sleep(2000);
                    String stringLat = String.valueOf(latitude);
                    String stringLong=String.valueOf(longitude);
                    //Toast.makeText(getApplicationContext(), stringLat+stringLong, Toast.LENGTH_SHORT).show();

                    if (mPref.getString("LATLNG", "").matches("")) {
                        medit.putString("LATLNG", "Value").commit();
                        medit.putString("LatitudeA", Double.valueOf(latitude).toString()).commit();
                        medit.putString("LongitudeA",Double.valueOf(longitude).toString()).commit();

                        String latitudeString = mPref.getString("LatitudeA", "0");
                        String longitudeString=mPref.getString("LongitudeA","0");
                        lattitudeA = Double.parseDouble(latitudeString);
                        longitudeA=Double.parseDouble(longitudeString);
                        Date currentDate=new Date();
                        LatLng lt=new LatLng(lattitudeA,longitudeA);

                        createJsonObject(lt,currentDate);

                        locationA = new Location("point A");
                        locationA.setLatitude(lattitudeA);
                        locationA.setLongitude(longitudeA);

                    } else {

                        locationB = new Location("point B");
                        locationB.setLatitude(latitude);
                        locationB.setLongitude(longitude);
                        double distance = locationA.distanceTo(locationB);
                        Toast.makeText(getApplicationContext(),"Distance"+distance, Toast.LENGTH_SHORT).show();
                        if(10<distance) {

                            medit.putString("LatitudeA","").commit();
                            medit.putString("LongitudeA","").commit();
                            medit.putString("LatitudeA", Double.valueOf(latitude).toString()).commit();
                            medit.putString("LongitudeA",Double.valueOf(longitude).toString()).commit();

                            String latitudeString = mPref.getString("LatitudeA", "0");
                            String longitudeString=mPref.getString("LongitudeA","0");
                            lattitudeA = Double.parseDouble(latitudeString);
                            longitudeA=Double.parseDouble(longitudeString);

                            locationA = new Location("point A");
                            locationA.setLatitude(lattitudeA);
                            locationA.setLongitude(longitudeA);


                            Toast.makeText(getApplicationContext(),"Post Date", Toast.LENGTH_SHORT).show();

                            LatLng latLng = new LatLng(latitude, longitude);
                            Date currentDate = new Date();
                            createJsonObject(latLng, currentDate);
                        }
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

                mHandler.postDelayed(this, 5000);

            }
        }, 5000);
    }



    ///////////////////// JSON Date Post Start
    public void createJsonObject(LatLng latLng,Date date) {
        jsonObject = new JSONObject();
        //  JSONObject locationObject = new JSONObject();
        JSONArray jsonArray=new JSONArray();
        try {
            jsonObject.put("date",date);
            jsonObject.put("latitude",location.getLatitude());
            jsonObject.put("longitude",location.getLongitude());
            jsonObject.put("deviceId",id.toString());
            new TrackLocation().execute();

        } catch (JSONException e) {
            Log.e("Error:", e.toString());
        }
    }


    class TrackLocation extends AsyncTask<Void, Void, JSONObject> {

        String URL = URLConstants.Track_Update;

        @Override
        protected JSONObject doInBackground(Void... voids) {
            JSONObject result;
            result =postJsonObject(URL, jsonObject);
            return result;
        }
    }

    public JSONObject postJsonObject(String url, JSONObject addJobj) {

//		InputStream inputStream = null;
        String result = "";

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Request request1 = request.newBuilder().header("Accept", "application/json")
                        .header("Content-Type", "application/json").build();

                return chain.proceed(request1);
            }
        });

        OkHttpClient httpClient1 =httpClient.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(" http://2b951580.ngrok.io/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient1)
                .build();

        Gps_api gps_api = retrofit.create(Gps_api.class);
        Model model = null;
        try {
            String username = addJobj.getString("username");
            String password = addJobj.getString("password");
            String deviceId = addJobj.getString("deviceId");
            String role= addJobj.getString("role");
            String refreshToken = addJobj.getString("refreshToken");

            model = new Model(username,password,deviceId,role,refreshToken);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        gps_api.reguser(model.getUsername(),model.getPassword(),model.getDeviceId(),model.getRole(),model.getRefreshToken()).enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, retrofit2.Response<Model> response) {
                Log.d("InputStream ",response.toString() );


            }

            @Override
            public void onFailure(Call<Model> call, Throwable t) {
                t.printStackTrace();
            }
        });
        JSONObject json = null;

        try {
            json = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Log.d("JSON-->", json.toString());
        return json;
    }




}

