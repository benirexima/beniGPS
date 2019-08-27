package com.example.benigps;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.benigps.utils.URLConstants;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

public class MapService extends Service {

    Location location;
    double latitude,lattitudeA,lattitudeB;
    double longitude,longitudeA,longitudeB;
    Location locationA;
    String id;
    Location locationB;
    JSONObject jsonObject;
    LatLng commondLatLng;
    Handler mHandler = new Handler();
    public Runnable myRunnable;
    private static final int NOTIFICATION_ID = 101;
    public NotificationManager notificationManager;
    private Notification n;

    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        Intent intent1 = new Intent(this, User_Class.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent1, 0);
        n  = new Notification.Builder(this)
                .setContentTitle("GPS Tracker")
                .setTicker("GPS Tracking started")
                .setContentText("Gps Tracking your location")
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.common_google_signin_btn_icon_dark_normal, "View more", pIntent).build();

        notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //  n.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        n.flags |=Notification.FLAG_NO_CLEAR;
        notificationManager.notify(NOTIFICATION_ID, n);

        id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("Mapservice"," onCreate service called>>>>>>>>>>>>>>>service");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3500);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);

        Log.d("Mapservice"," onStart service called>>>>>>>>>>>>>>>service");

        RefreshUserLocation();

    }

    @Override
    public void onDestroy() {
        notificationManager.cancel(NOTIFICATION_ID);

        mHandler.removeCallbacks(myRunnable);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        locationManager.removeUpdates(listener);
        locationManager = null;
        stopSelf();
        Toast.makeText(this, "Tracking Stopped", Toast.LENGTH_LONG).show();
        Log.d("Mapservice"," onDestroy service called>>>>>>>>>>>>>>>service");
    }
    @Override
    public void onStart(final Intent intent, int startId) {



        Toast.makeText(getApplicationContext(),"Tracking Started",Toast.LENGTH_SHORT).show();
    }

    public class MyLocationListener implements LocationListener
    {

        public void onLocationChanged(final Location loc)
        {
            Log.i("Mapservice", loc.getSpeed() +" Location changed "+loc.getAccuracy());

            loc.getLatitude();
            loc.getLongitude();

            if(loc!=null) {
                commondLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());

            }
            else
            {
                Toast.makeText(MapService.this, "Null value", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        public void onProviderDisabled(String provider)
        {

        }


        public void onProviderEnabled(String provider)
        {
            //Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }

    }

    public void RefreshUserLocation() {

        mHandler.postDelayed(myRunnable=new Runnable() {
            @Override
            public void run() {


                try {
                    Date currentDate = new Date();

                    createJsonObject(commondLatLng, currentDate);
                    Toast.makeText(getApplicationContext(),"Post Data",Toast.LENGTH_SHORT).show();


                } catch (Exception e)

                {
                    e.printStackTrace();
                }

                mHandler.postDelayed(this,10000);

            }
        }, 10000);

    }


    public void createJsonObject(LatLng latLng,Date date) {
        jsonObject = new JSONObject();
        //  JSONObject locationObject = new JSONObject();
        JSONArray jsonArray=new JSONArray();
        try {

            jsonObject.put("date",date);
            jsonObject.put("latitude",latLng.latitude);
            jsonObject.put("longitude",latLng.longitude);
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

        InputStream inputStream = null;
        String result = "";



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

