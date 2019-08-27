package com.example.benigps;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.benigps.Model.Model;
import com.example.benigps.service.Gps_api;
import com.example.benigps.service.ServiceCall;
import com.example.benigps.utils.URLConstants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class User_Class extends AppCompatActivity {
    JSONObject jsonObject;
    String id;
    String inTime;
    Button btnStart,btnStop;
    String userinfo;
    boolean result=false;
    private static final String TAG = MainActivity.class.getSimpleName();
    SharedPreferences mmmPref,mUser,getUser;

    SharedPreferences.Editor mmmedit,mUseEdit;

    private GoogleApiClient googleApiClient;
    private ProgressDialog pDialog;
    final static int REQUEST_LOCATION = 199;
    boolean check_mobile_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_class);
        btnStart = (Button) findViewById(R.id.StartTrack);
        btnStop = (Button) findViewById(R.id.StopService);

        getUser  = getSharedPreferences("LoginDetails" , Context.MODE_PRIVATE);

        mmmPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mmmedit = mmmPref.edit();
        mUser=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mUseEdit=mUser.edit();
        id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        //Actionbar titile
        new GetMobileID().execute();

        userinfo=getUser.getString("user_name","et");
        TextView showName=(TextView)findViewById(R.id.showUser);
        showName.setText("Welcome "+userinfo);

        if (mmmPref.getString("service", "").matches(""))
        {
            // btnStop.setEnabled(false);
            //btnStart.setEnabled(true);
            btnStop.setVisibility(View.GONE);
            btnStart.setVisibility(View.VISIBLE);
        }
        else
        {
            // btnStart.setEnabled(false);
            //btnStop.setEnabled(true);
            btnStop.setVisibility(View.VISIBLE);
            btnStart.setVisibility(View.GONE);

        }
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    if (!hasGPSDevice(User_Class.this)) {
                        Toast.makeText(User_Class.this, "Gps not Supported", Toast.LENGTH_SHORT).show();
                    }

                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(User_Class.this)) {
                        enableLoc();
                    }
                    // turnGPSOn();

                } else {
                    if (isNetworkAvailable()) {


                        if (mmmPref.getString("service", "").matches("")) {
                            mmmedit.putString("service", "service").commit();
                            startNotification();

                            //  btnStop.setEnabled(true);
                            //btnStart.setEnabled(false);
                            btnStop.setVisibility(View.VISIBLE);
                            btnStart.setVisibility(View.GONE);
                            Date currentDate = new Date();
                            createJsonObject(currentDate, null);
                            startService(new Intent(getApplicationContext(), MapService.class));
                            Intent intent = new Intent(getApplicationContext(), MapService.class);
                            startService(intent);

                        } else {
                            Toast.makeText(getApplicationContext(), "Service is already running", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "Internet connection not available", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            //}
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // btnStop.setEnabled(false);
                //btnStart.setEnabled(true);
                btnStop.setVisibility(View.GONE);
                btnStart.setVisibility(View.VISIBLE);
                Date currentDate = new Date();
                createJsonObjectOUTTIME(null,currentDate);
                //   createJsonObject(null, currentDate);
                mmmedit.putString("service", "").commit();
                result=false;
                stopService(new Intent(getApplicationContext(), MapService.class));
                //Send notification
                sendNotification();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);//Menu Resource, Menu
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:

                // Toast.makeText(getApplicationContext(),"Item 1 Selected",Toast.LENGTH_LONG).show();
                return true;
            case R.id.item2:
                //Toast.makeText(getApplicationContext(),"Item 2 Selected",Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    ///Turn on GPS Programatically

    private void turnGPSOn()
    {
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(!provider.contains("gps"))
        { //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }
    }

    private boolean hasGPSDevice(Context context) {
        final LocationManager mgr = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null)
            return false;
        final List<String> providers = mgr.getAllProviders();
        if (providers == null)
            return false;
        return providers.contains(LocationManager.GPS_PROVIDER);
    }

    private void enableLoc() {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.d("Location error","Location error " + connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(User_Class.this, REQUEST_LOCATION);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                    }
                }
            });
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        finish();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }

    }
    ///////////////////////////////
    public void createJsonObject(Date inTime, Date outTime) {

        Toast.makeText(getApplicationContext(),"Please wait...",Toast.LENGTH_SHORT).show();
        new GetMobileID().execute();
        try {
            Thread.sleep(3000);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        jsonObject = new JSONObject();
        JSONObject locationObject = new JSONObject();
        if (!check_mobile_id) {
            try {
                jsonObject.put("username", userinfo.toString());
                jsonObject.put("deviceId", id);
                jsonObject.put("inTime", inTime);
                //jsonObject.put("outTime", null);
                new SaveUserData().execute();
            } catch (JSONException e) {
                Log.e("Error:", e.toString());
            }
        }else{
            try {
                jsonObject.put("deviceId", id);
                jsonObject.put("inTime", inTime);
                new UpdateInTime().execute();


            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }

    public void createJsonObjectOUTTIME(Date inTime, Date outTime) {

        // new GetMobileID().execute();
        jsonObject = new JSONObject();
        try {
            jsonObject.put("deviceId", id);
            jsonObject.put("outTime", outTime);
            new UpdateUserOutTime().execute();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    class SaveUserData extends AsyncTask<Void, Void, JSONObject> {

        String URL = URLConstants.Track;

        @Override
        protected JSONObject doInBackground(Void... voids) {
           JSONObject result = null;
//            try {
//                result = postJsonObject(URL, jsonObject);
//                inTime = result.getString("inTime");
//            } catch (JSONException e) {
//                Log.e(TAG, "doInBackground: JSON Error");
//            }
//            return result;


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
                String username = jsonObject.getString("username");
                String password = jsonObject.getString("password");
                String deviceId = jsonObject.getString("deviceId");
                String role= jsonObject.getString("role");
                String refreshToken = jsonObject.getString("refreshToken");

                model = new Model(username,password,deviceId,role,refreshToken);

            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                Model model1 = gps_api.reguser(model.getUsername(),model.getPassword(),model.getDeviceId(),model.getRole(),model.getRefreshToken()).execute().body();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", model1.getUsername());
                jsonObject.put("password", model1.getPassword());
                jsonObject.put("deviceId", model1.getDeviceId());
                jsonObject.put("role",model1.getRole());
                jsonObject.put("refreshToken",model1.getRefreshToken());


                return jsonObject;

            } catch (Exception e) {
                e.printStackTrace();

                return null;
            }

        }
    }

    class UpdateUserOutTime extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... voids) {
            return postJsonObjectLocation(URLConstants.Update_OutTime, jsonObject);
        }
    }
    class UpdateInTime extends AsyncTask<Void,Void,JSONObject>
    {
        @Override
        protected JSONObject doInBackground(Void... voids)
        {
            return  postJsonObjectLocation(URLConstants.Update_InTime,jsonObject);

        }
    }


    public JSONObject postJsonObjectLocation(String url, JSONObject addJobj) {




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
                .baseUrl("http://2b951580.ngrok.io/")
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

        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        try {

            Model model1 = gps_api.reguser(model.getUsername(),model.getPassword(),model.getDeviceId(),model.getRole(),model.getRefreshToken()).execute().body();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", model1.getUsername());
            jsonObject.put("password", model1.getPassword());
            jsonObject.put("deviceId", model1.getDeviceId());
            jsonObject.put("role",model1.getRole());
            jsonObject.put("refreshToken",model1.getRefreshToken());


            return jsonObject;
        }
            catch (Exception e) {
                e.printStackTrace();

                return null;

            }


    }


    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private class GetMobileID extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog

            pDialog = new ProgressDialog(User_Class.this);
            pDialog.setMax(5);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
            Runnable progressRunnable = new Runnable() {

                @Override
                public void run() {
                    pDialog.cancel();
                }
            };

            Handler pdCanceller = new Handler();
            pdCanceller.postDelayed(progressRunnable, 5000);

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Gps_api gps_api = ServiceCall.getInstance().getRetroObject().create(Gps_api.class);

            // Making a request to url and getting response
            String jsonStr = null;
            try {
               Model model = gps_api.postTrack(userinfo, id, inTime).execute().body();
               // ResponseBody responseBody = JSONArray.body();

                jsonStr = new Gson().toJson(model);
                Log.d("josnArray",model.toString()+"  "+model.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {

                    JSONArray jsonArray = new JSONArray(jsonStr);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject c = jsonArray.getJSONObject(i);

                        String mobile = c.getString("deviceId");

                        if(mobile.equals(id))
                        {
                            check_mobile_id=true;
                        }


                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't connect server.Server has been stopped!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();

        }

    }

    public void startNotification()
    {
        String deviceID= Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        jsonObject=new JSONObject();
        Date date1=new Date();
        String str=String.valueOf(date1.toString());

        try {
            SimpleDateFormat format = new SimpleDateFormat("MMM dd,yyyy  hh:mm a");
            String date = format.format(Date.parse(str));

            jsonObject.put("deviceId", deviceID.toString());
            String msg=userinfo.toString()+" Start at "+date;
            jsonObject.put("date",date.toString());
            jsonObject.put("message",msg);
            new SendNotification().execute();


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void sendNotification()
    {
        String deviceID= Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        jsonObject=new JSONObject();
        Date date2=new Date();
        String str=String.valueOf(date2.toString());

        try {
            SimpleDateFormat format = new SimpleDateFormat("MMM dd,yyyy  hh:mm a");
            String date = format.format(Date.parse(str));

            jsonObject.put("deviceId", deviceID.toString());
            String msg=userinfo.toString()+" Stopped at "+date.toString();
            jsonObject.put("date",date.toString());
            jsonObject.put("message",msg);
            new SendNotification().execute();


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    class SendNotification extends AsyncTask<Void, Void, JSONObject> {

        String URL=URLConstants.Send_Notify;
        @Override
        protected JSONObject doInBackground(Void... voids) {
            JSONObject result;
            result =postJsonObjectLocation(URL, jsonObject);
            return result;
        }
    }
}




